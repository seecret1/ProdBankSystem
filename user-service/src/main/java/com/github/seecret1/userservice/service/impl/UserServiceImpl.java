package com.github.seecret1.userservice.service.impl;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.CreateUserRequest;
import com.github.seecret1.userservice.dto.request.UpdateUserRequest;
import com.github.seecret1.userservice.dto.response.UserResponse;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.enums.UserStatus;
import com.github.seecret1.userservice.exception.*;
import com.github.seecret1.userservice.mapper.UserMapper;
import com.github.seecret1.userservice.model.UserFilterModel;
import com.github.seecret1.userservice.repository.UserRepository;
import com.github.seecret1.userservice.repository.specification.UserSpecification;
import com.github.seecret1.userservice.service.InternalUserService;
import com.github.seecret1.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, InternalUserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Value("${services.api-key}")
    private String internalApiKey;

    @Override
    @Cacheable(value = "${app.cache.cache-names.userAll}", key = "#pageModel.toString()")
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAllUsers(PageModel pageModel) {
        log.info("Find all users");

        Pageable pageable = pageModel.toPageRequest();
        var page = userRepository.findAll(pageable);
        log.debug("Find users list. page: {}, size list: {}, page list: {}",
                page.getTotalPages(), page.getTotalElements(), page.getContent());

        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                userMapper.toListResponse(page.getContent())
        );
    }

    @Override
    @Cacheable(value = "${app.cache.cache-names.userActiveAll}", key = "#pageModel.toString()")
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAllActiveUsers(PageModel pageModel) {
        log.info("Find all active users");

        Pageable pageable = pageModel.toPageRequest();
        var page = userRepository.findAllActiveUsers(pageable);
        log.debug("Find active users list. page: {}, size list: {}, page list: {}",
                page.getTotalPages(), page.getTotalElements(), page.getContent());

        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                userMapper.toListResponse(page.getContent())
        );
    }

    @Override
    @Cacheable(value = "${app.cache.cache-names.userFilter}", key = "#filter.toString()")
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findByFilter(UserFilterModel filter) {
        log.info("Find users by filter: {}", filter);

        var page = userRepository.findAll(
                UserSpecification.withFilter(filter),
                filter.getPage().toPageRequest()
        );
        log.debug("Find page by filter, Page: {}", page);
        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                userMapper.toListResponse(page.getContent())
        );
    }

    @Override
    @Cacheable(value = "${app.cache.cache-names.userById}", key = "#id")
    @Transactional(readOnly = true)
    public UserResponse findById(String id) {
        log.info("Find user by id: {}", id);
        var user = findUserEntityById(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Cacheable(value = "${app.cache.cache-names.userByEmail}", key = "#email")
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        log.info("Find user by email: {}", email);
        var user = findUserEntityByEmail(email);
        return userMapper.toResponse(user);
    }

    @Override
    @Cacheable(value = "${app.cache.cache-names.userByUsername}", key = "#username")
    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        log.info("Find user by username: {}", username);
        var user = findUserEntityByUsername(username);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(String id, String apiKey) {

        log.info("Check api key with internal api key");
        if (!internalApiKey.equals(apiKey)) {
            throw new SecurityException("Invalid internal API key");
        }
        var response = findById(id);
        if (response.status() != UserStatus.ACTIVE) {
            throw new PersonException("User status not ACTIVE");
        }
        return response;
    }

    @Override
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.userAll}",
                    "${app.cache.cache-names.userActiveAll}",
                    "${app.cache.cache-names.userFilter}",
                    "${app.cache.cache-names.userById}",
                    "${app.cache.cache-names.userByEmail}",
                    "${app.cache.cache-names.userByUsername}"
            },
            allEntries = true
    )
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponse create(CreateUserRequest request) {
        log.info("Call method create");

        var username = request.username();
        var email = request.email();

        if (userRepository.existsByUsernameOrEmail(username, email)) {
            throw new RegisterUserException(
                    MessageFormat.format(
                            "User by username {0} or email {1} exists",
                            username, email
                    )
            );
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        log.debug("Success create user: {}", user);
        return userMapper.toResponse(user);
    }

    @Override
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.userAll}",
                    "${app.cache.cache-names.userActiveAll}",
                    "${app.cache.cache-names.userFilter}",
                    "${app.cache.cache-names.userByEmail}",
                    "${app.cache.cache-names.userByUsername}"
            },
            allEntries = true
    )
    @CachePut(value = "${app.cache.cache-names.userById}", key = "#id")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponse updateFull(String id, CreateUserRequest request) {
        log.info("Full update user by id: {}", id);

        User existingUser = findUserEntityById(id);

        existingUser.setUsername(request.username());
        existingUser.setStatus(request.status());
        existingUser.setEmail(request.email());
        existingUser.setPassword(passwordEncoder.encode(request.password()));
        existingUser.setFirstName(request.firstName());
        existingUser.setLastName(request.lastName());
        existingUser.setMiddleName(request.middleName());
        existingUser.setBirthDate(request.birthDate());
        existingUser.setRole(request.role());

        User savedUser = userRepository.save(existingUser);

        log.debug("Success full update user: {}", savedUser);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.userAll}",
                    "${app.cache.cache-names.userActiveAll}",
                    "${app.cache.cache-names.userFilter}",
                    "${app.cache.cache-names.userByEmail}",
                    "${app.cache.cache-names.userByUsername}"
            },
            allEntries = true
    )
    @CachePut(value = "${app.cache.cache-names.userById}", key = "#userId")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponse updateYour(String userId, UpdateUserRequest request) {
        log.info("Update user by id: {}", userId);

        var userUpdate = findUserEntityById(userId);
        try {
            if (request.username() != null) {
                userUpdate.setUsername(request.username());
            }
            if (request.email() != null) {
                userUpdate.setEmail(request.email());
            }

            userRepository.save(userUpdate);
            log.debug("Success update user: {}", userUpdate);
            return userMapper.toResponse(userUpdate);

        } catch (DataIntegrityViolationException ex) {
            throw new PersonDataExistsException("Person data updated exists: " + ex.getMessage());
        }
    }

    @Override
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.userAll}",
                    "${app.cache.cache-names.userActiveAll}",
                    "${app.cache.cache-names.userFilter}",
                    "${app.cache.cache-names.userById}",
                    "${app.cache.cache-names.userByEmail}",
                    "${app.cache.cache-names.userByUsername}"
            },
            allEntries = true
    )
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(String deletedBy, String id) {
        log.info("Delete user by id: {}", id);
        var user = findUserEntityById(id);
        log.debug("Success delete user: {}", user);

        user.softDelete(deletedBy);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserEntityById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found by id: " + id
                ));
    }

    @Override
    public User findUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found by email: " + email
                ));
    }

    @Override
    public User findUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found by username: " + username
                ));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void saveUser(User user) {
        log.info("Save user: {}", user);
        userRepository.save(user);
        log.debug("Success save user: {}", user);
    }
}

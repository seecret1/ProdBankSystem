package com.github.seecret1.userservice.service.impl;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.CreateUserRequest;
import com.github.seecret1.userservice.dto.request.UpdateUserRequest;
import com.github.seecret1.userservice.dto.response.UserResponse;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.exception.AuthException;
import com.github.seecret1.userservice.exception.RegisterUserException;
import com.github.seecret1.userservice.exception.UserNotFoundException;
import com.github.seecret1.userservice.mapper.UserManualMapper;
import com.github.seecret1.userservice.model.UserFilterModel;
import com.github.seecret1.userservice.repository.UserRepository;
import com.github.seecret1.userservice.repository.specification.UserSpecification;
import com.github.seecret1.userservice.service.InternalUserService;
import com.github.seecret1.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final UserManualMapper userManualMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
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
                userManualMapper.toListResponse(page.getContent())
        );
    }

    @Override
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
                userManualMapper.toListResponse(page.getContent())
        );
    }

    @Override
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
                userManualMapper.toListResponse(page.getContent())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByCriterial(String criterial) {
        log.info("Find user by criterial: {}", criterial);
        User user = userRepository.findByCriterial(criterial)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found by criterial: " + criterial
                ));
        log.debug("Found user by criterial. User: {}", user);
        return userManualMapper.toResponse(user);
    }

    @Override
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
        User user = userManualMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        log.debug("Success create user: {}", user);
        return userManualMapper.toResponse(user);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponse updateFull(String criterial, CreateUserRequest request) {
        log.info("Full update user by criterial: {}", criterial);

        User existingUser = userRepository.findByCriterial(criterial)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with criterial: " + criterial
                ));

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
        return userManualMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponse updateYour(String userId, UpdateUserRequest request) {
        log.info("Update user by id: {}", userId);

        var userUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId
                ));
        try {
            if (request.username() != null) {
                userUpdate.setUsername(request.username());
            }
            if (request.email() != null) {
                userUpdate.setEmail(request.email());
            }
            if (request.password() != null) {
                userUpdate.setPassword(passwordEncoder.encode(request.password()));
            }

            userRepository.save(userUpdate);
            log.debug("Success update user: {}", userUpdate);
            return userManualMapper.toResponse(userUpdate);

        } catch (DataIntegrityViolationException ex) {
            throw new AuthException(ex.getMessage());
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(String deletedBy, String criterial) {
        log.info("Delete user by criterial: {}", criterial);
        var user = userRepository.findByCriterial(criterial)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found by criterial: " + criterial
                ));
        log.debug("Success delete user: {}", user);

        user.softDelete(deletedBy);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserEntityByCriterial(String criterial) {
        log.info("Find user entity by criterial: {}", criterial);
        User user = userRepository.findByCriterial(criterial)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found by criterial: " + criterial
                ));
        log.debug("Found user entity by criterial. User: {}", user);
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserEntityById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
}

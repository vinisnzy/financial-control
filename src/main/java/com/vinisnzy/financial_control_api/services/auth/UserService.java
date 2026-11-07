package com.vinisnzy.financial_control_api.services.auth;

import com.vinisnzy.financial_control_api.dtos.user.requests.UpdateUserRequest;
import com.vinisnzy.financial_control_api.dtos.user.responses.UserResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.UserMapper;
import com.vinisnzy.financial_control_api.repositories.auth.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    private UserService(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public User getEntity(UUID id) {
        return this.repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", id));
    }

    public UserResponse getById(UUID id) {
        return this.mapper.toResponse(this.getEntity(id));
    }

    public UserResponse getByEmail(String email) {
        return this.mapper.toResponse(this.repository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> NotFoundException.of("User", email)));
    }

    public UserResponse update(UUID id, UpdateUserRequest data) {
        User entity = this.getEntity(id);
        this.mapper.updateEntity(data, entity);
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID id) {
        this.repository.deleteById(id);
    }
}

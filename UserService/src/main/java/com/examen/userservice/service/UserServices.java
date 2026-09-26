package com.examen.userservice.service;

import com.examen.userservice.domain.UserMaster;
import com.examen.userservice.dto.UserRequestDto;
import com.examen.userservice.dto.UserResponseDto;
import com.examen.userservice.repository.UserMasterRepo;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServices {
    private UserMasterRepo repo;
    private ModelMapper mapper;

    public UserResponseDto save(UserRequestDto userRequestDto){
        return mapper.map(repo.save(mapper.map(userRequestDto, UserMaster.class)), UserResponseDto.class);
    }

    public List<UserResponseDto> getAll() {
        return mapper.map(repo.findAll(),new TypeToken<List<UserResponseDto>>() {}.getType());
    }

    public UserResponseDto getById(Long id) {
        return mapper.map(repo.findById(id).orElseThrow(()-> new IllegalArgumentException("Id Not found")), UserResponseDto.class);
    }
}

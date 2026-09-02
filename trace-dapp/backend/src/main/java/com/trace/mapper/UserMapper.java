package com.trace.mapper;

import com.trace.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    List<User> findAll();
    User findById(@Param("id") Long id);
    User findByUsername(@Param("username") String username);
    List<User> findByRole(@Param("role") Integer role);
    int insert(User user);
    int update(User user);
}
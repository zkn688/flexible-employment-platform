package com.example.employment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.employment.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

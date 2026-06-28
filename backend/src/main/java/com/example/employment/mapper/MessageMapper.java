package com.example.employment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.employment.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}

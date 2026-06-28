package com.example.employment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.employment.entity.Company;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CompanyMapper extends BaseMapper<Company> {
}

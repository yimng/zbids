package com.caicui.app.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.caicui.app.entity.Student;
import com.caicui.app.mapper.StudentMapper;
import com.caicui.app.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    private StudentMapper studentMapper;

    @Override
    public String insertStudent(Student student) {
        student.setModifyDate(new Date());
        studentMapper.insert(student);
        return student.getStudentId();
    }

    @Override
    public Student selectStudentById(String id) {
        return studentMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateStudent(Student student) {
        return studentMapper.updateByPrimaryKeySelective(student);
    }
}

package com.lec.spring.domain;

import com.lec.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    @Autowired
    UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {

        return User.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;

//        username 은 필수, 중복된 username 여부
        String username = user.getUsername();
        if (username == null || username.trim().isEmpty()){
            errors.rejectValue("username", "username은 필수입니다.");
        } else if (userService.isExist(username)) {
            errors.rejectValue("username", "이미 존재하는 아이디(username)입니다.");
        }

//        email
//        입력이 되어 있으면 정규표현식 패턴 체크


//        입력 password, re_password 가 동일한지 비교
        if (!user.getPassword().equals(user.getRe_password())){
            errors.rejectValue("re_password","비밀번호와 비밀번호확인 입력값은 같아야 합니다.");
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "re_password","비밀번호는 공란이 될수 없습니다.");

    }
}

package com.demoproject.interceptor;


import com.demoproject.entity.Account;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import com.demoproject.entity.Users;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Map;
import java.util.Optional;

public class UserProfileInterceptor implements HandlerInterceptor {

    private final UserService userService;
    private final AccountService accountService;

    public UserProfileInterceptor(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Lấy thông tin user hiện tại từ session hoặc security context
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }
        Account account= accountService.getAccountFromToken(token).orElse(null);


        Optional<Users> user = userService.getUserProfile(account.getUserId()); // Giả định có hàm lấy user hiện tại

        if (user.isPresent()) {
            Users currentUser = user.get();
            if (currentUser.getName() == null || currentUser.getPhone() == null ||
                    currentUser.getGender() == null  ||
                    currentUser.getDateOfBirth() == null) {

                // Lưu thông báo vào session để hiển thị trên trang userprofile
                HttpSession session = request.getSession();
                session.setAttribute("alertMessage", "Bạn phải nhập đủ thông tin đã");

                // Chuyển hướng về trang user profile
                response.sendRedirect(request.getContextPath() + "/user/userprofile");
                return false;
            }
        }
        return true;
    }
}


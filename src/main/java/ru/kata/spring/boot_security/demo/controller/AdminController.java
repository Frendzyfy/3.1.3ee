package ru.kata.spring.boot_security.demo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Controller
@RequestMapping("/admin")
public class AdminController {


    @Autowired
    private RoleRepository roleRepository;

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showAllUsers(Model model) {

        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);

        return "users";
    }
    @GetMapping("/addNewUser")
    public String addNewUser(Model model) {
        User user = new User();
        List<Role> list = roleRepository.findAll();
        model.addAttribute("newUser", user);
        model.addAttribute("allRoles", list);
        return "user-info";
    }
    @PostMapping("/saveUser")
    public String saveUser(@RequestParam("roles") List<Long> roleIds, @ModelAttribute("newUser") @Valid User user, BindingResult bindingResult, Model model, HttpServletRequest request) {
        Optional<User> optionalUser = userService.getUserByUsername(user.getUsername());
//        if (!optionalUser.isEmpty() & request.getRequestURI().equals("/addNewUser") || !optionalUser.isEmpty() & optionalUser.get() != user.getUsername() &)
        if (!optionalUser.isEmpty() && optionalUser.get().getId() != user.getId())
            bindingResult.rejectValue("username", "error.username", "User with the same username already exists");
        if (bindingResult.hasErrors()) {
            List<Role> list = roleRepository.findAll();
            model.addAttribute("allRoles", list);
            return "user-info";
        } else {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            user.setRoles(roles);
            userService.saveUser(user);
            return "redirect:/admin";
        }
    }
    @GetMapping("/updateInfo")
    public String updateUser(@RequestParam("userId") Long id, Model model) {
        User user = userService.getUser(id);
        List<Role> list = roleRepository.findAll();
        model.addAttribute("newUser", user);
        model.addAttribute("allRoles", list);
        return "user-info";
    }
    @GetMapping("/deleteUser")
    public String deleteUser(@RequestParam("userId") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}
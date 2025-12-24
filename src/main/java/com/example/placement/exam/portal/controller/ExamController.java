package com.example.placement.exam.portal.controller;

import com.example.placement.exam.portal.model.Question;
import com.example.placement.exam.portal.model.User;
import com.example.placement.exam.portal.repository.QuestionRepository;
import com.example.placement.exam.portal.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class ExamController {

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping("/")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        userService.registerUser(user);
        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "Invalid Credentials");
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/";
        return "dashboard";
    }

    @GetMapping("/exam/{category}")
    public String startExam(@PathVariable String category, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/";
        List<Question> questions = questionRepository.findByCategory(category);
        model.addAttribute("questions", questions);
        model.addAttribute("category", category);
        return "exam";
    }

    @PostMapping("/submit-exam")
    public String submitExam(@RequestParam Map<String, String> allParams, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/";

        // 1. Identify which category was being tested (hidden field from exam.html)
        String category = allParams.get("category");
        if (category == null) {
            return "redirect:/dashboard";
        }
        
        // 2. Fetch questions for that category
        List<Question> questions = questionRepository.findByCategory(category);
        
        int totalQuestions = questions.size();
        int correct = 0;
        int answeredCount = 0;

        // 3. Loop through questions and check answers
        for (Question q : questions) {
            String submittedAnswer = allParams.get("question_" + q.getId());
            
            if (submittedAnswer != null && !submittedAnswer.isEmpty()) {
                answeredCount++;
                if (Integer.parseInt(submittedAnswer) == q.getCorrectAnswer()) {
                    correct++;
                }
            }
        }

        // 4. Calculate detailed statistics
        int incorrect = answeredCount - correct;
        int skipped = totalQuestions - answeredCount;
        double percentage = (totalQuestions > 0) ? ((double) correct / totalQuestions) * 100 : 0;
        
        // Pass if 50% or more
        String status = (percentage >= 50.0) ? "PASS" : "FAIL";

        // 5. Add attributes to Model (Compatible with result.html)
        model.addAttribute("status", status);
        model.addAttribute("percentage", String.format("%.1f", percentage));
        model.addAttribute("score", correct); // Compatibility for simple result page
        model.addAttribute("correct", correct); // Used in professional result page
        model.addAttribute("total", totalQuestions);
        model.addAttribute("incorrect", incorrect);
        model.addAttribute("skipped", skipped);
        model.addAttribute("category", category);

        return "result";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
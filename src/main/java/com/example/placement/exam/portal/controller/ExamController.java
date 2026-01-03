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

        // IMPORTANT UPDATE: Use IgnoreCase for reliable cloud searching
        List<Question> questions = questionRepository.findByCategoryIgnoreCase(category);
        
        // DEBUG LOGS: Check Render "Logs" tab to see these values
        System.out.println("--- Exam Start Debug ---");
        System.out.println("Requested Category: " + category);
        System.out.println("Questions Found in DB: " + (questions != null ? questions.size() : 0));

        model.addAttribute("questions", questions);
        model.addAttribute("category", category);
        return "exam";
    }

    @PostMapping("/submit-exam")
    public String submitExam(@RequestParam Map<String, String> allParams, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/";

        String category = allParams.get("category");
        if (category == null) {
            return "redirect:/dashboard";
        }
        
        // IMPORTANT UPDATE: Use IgnoreCase here as well
        List<Question> questions = questionRepository.findByCategoryIgnoreCase(category);
        
        int totalQuestions = questions.size();
        int correct = 0;
        int answeredCount = 0;

        for (Question q : questions) {
            String submittedAnswer = allParams.get("question_" + q.getId());
            
            if (submittedAnswer != null && !submittedAnswer.isEmpty()) {
                answeredCount++;
                if (Integer.parseInt(submittedAnswer) == q.getCorrectAnswer()) {
                    correct++;
                }
            }
        }

        int incorrect = answeredCount - correct;
        int skipped = totalQuestions - answeredCount;
        double percentage = (totalQuestions > 0) ? ((double) correct / totalQuestions) * 100 : 0;
        
        String status = (percentage >= 50.0) ? "PASS" : "FAIL";

        model.addAttribute("status", status);
        model.addAttribute("percentage", String.format("%.1f", percentage));
        model.addAttribute("score", correct); 
        model.addAttribute("correct", correct); 
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
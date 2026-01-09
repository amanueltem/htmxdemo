package com.aman.htmxdemo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TaskController {

    private final List<String> tasks = new ArrayList<>(List.of("Learn Java 25", "Master HTMX"));

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tasks", tasks);
        return "index";
    }

    @PostMapping("/add-task")
    public String addTask(@RequestParam String taskName, Model model) {
        tasks.add(taskName);
        model.addAttribute("tasks", tasks);
        // Returns ONLY the fragment to update the list
        return "index :: task-list";
    }

    @DeleteMapping("/delete-task/{id}")
    @ResponseBody
    public String deleteTask(@PathVariable int id) {
        if (id >= 0 && id < tasks.size()) {
            tasks.remove(id);
        }
        // HTMX handles an empty response by removing the element if hx-swap is used correctly
        return "";
    }
}
//vAeHKrBGBHJo1adq
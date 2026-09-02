package com.evidence.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-path}")
    private String uploadPath;

    @GetMapping("/{year}/{month}/{day}/{filename}")
    public void getFile(@PathVariable String year, @PathVariable String month,
                        @PathVariable String day, @PathVariable String filename,
                        HttpServletResponse response) throws Exception {
        File file = new File(uploadPath + year + "/" + month + "/" + day + "/" + filename);
        if (!file.exists()) {
            response.setStatus(404);
            return;
        }
        response.setContentType("application/octet-stream");
        Files.copy(file.toPath(), response.getOutputStream());
        response.getOutputStream().flush();
    }
}
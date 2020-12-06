package br.com.pricardo.aws.s3;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/v1/s3")
public class S3Controller {

    private final S3ClientService s3ClientService;

    public S3Controller(S3ClientService s3ClientService) {
        this.s3ClientService = s3ClientService;
    }

    @GetMapping
    public List<S3ObjectData> consultar() {
        return this.s3ClientService.consultar();
    }

    @GetMapping("/download/{filename}")
    public @ResponseBody
    byte[] download(@PathVariable(value = "filename") String filename) throws IOException {
        return s3ClientService.download(filename);
    }

    @PostMapping("/upload")
    public ResponseEntity<S3ObjectData> uploadFile(@RequestPart(value = "file") MultipartFile file) {
        S3ObjectData s3ObjectData = s3ClientService.upload(file);
        return ResponseEntity
                .created(URI.create(String.format("/download/%s", s3ObjectData.getName())))
                .body(s3ObjectData);
    }

    @DeleteMapping("/delete/{filename}")
    public String deleteFile(@PathVariable(value = "filename") String filename) {
        return this.s3ClientService.delete(filename);
    }
}
package com.task.needmoretask.core.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.task.needmoretask.core.exception.Exception400;
import com.task.needmoretask.core.exception.Exception500;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
@Service
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile).orElseThrow(() -> new Exception500("MultipartFile -> File 전환 실패"));
        return upload(uploadFile, dirName);
    }

    private String upload(File uploadFile, String dirName) {
        String fileName = dirName + "/" + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) log.info("파일이 삭제되었습니다");
        else log.info("파일이 삭제되지 못했습니다");
    }

    private Optional<File> convert(MultipartFile file) throws UnsupportedEncodingException {
        UUID uuid = UUID.randomUUID();
        String originalFilename = file.getOriginalFilename();
        StringBuilder convertedFilename = new StringBuilder();
        log.debug(String.valueOf(originalFilename));
        if (originalFilename == null) throw new Exception400("profile", "프로필 이미지 업로드 실패");

        // 파일 이름에서 확장자 추출
        int extensionIndex = originalFilename.lastIndexOf(".");
        String extension = extensionIndex == -1 ? "" : originalFilename.substring(extensionIndex);

        log.debug("extensionIndex: " + extensionIndex + ", extension: " + extension);

        // 확장자가 포함된 파일 이름에서 순수 파일 이름 추출
        String name = originalFilename.replace(" ","").substring(0, extensionIndex == -1 ? originalFilename.length() : extensionIndex);
        name = Normalizer.normalize(name, Normalizer.Form.NFD); // 정규화
        name = name.replaceAll("[^\\p{ASCII}]", ""); // 영어 알파벳만 남기고 제거
        name = name.toLowerCase().substring(0, Math.min(5, name.length())); // 소문자 변환 및 길이 제한

        log.debug("줄인 name: " + name);

        // 변환된 파일 이름에 확장자 추가
        convertedFilename.append(name).append(extension);

        log.debug("확장자가 붙은 name: " + convertedFilename);

        return Optional.of(uuid.toString().substring(0,5)+convertedFilename).map(File::new).filter(f -> {
            try {
                return f.createNewFile();
            } catch (IOException e) {
                log.error(e.getMessage());
                return false;
            }
        }).map(f -> {
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(file.getBytes());
                return f;
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }
        });
    }

}
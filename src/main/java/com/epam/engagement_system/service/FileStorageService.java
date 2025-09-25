package com.epam.engagement_system.service;

import com.epam.engagement_system.configuration.FileStorageConfiguration;
import com.epam.engagement_system.exception.storage.StorageException;
import com.epam.engagement_system.exception.storage.StorageFileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "application/pdf");
    private final Path rootLocation;

    public FileStorageService(FileStorageConfiguration properties) {
        this.rootLocation = Paths.get(properties.getUploadDirectory()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage location", e);
        }
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Failed to store empty file.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new StorageException("Invalid file type. Only JPG, PNG, and PDF are allowed.");
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        String fileExtension = "";
        try {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } catch (Exception ignored) {}

        String storedFilename = UUID.randomUUID() + fileExtension;
        try (InputStream inputStream = file.getInputStream()) {
            Path destinationFile = this.rootLocation.resolve(Paths.get(storedFilename)).normalize();
            if (!destinationFile.startsWith(this.rootLocation)) {
                throw new StorageException("Cannot store file outside current directory.");
            }
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Successfully stored file {} in {}", file.getName(), rootLocation.getRoot());
            return storedFilename;
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    public Resource downloadFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                logger.info("Successfully sent file {} to download", file.getFileName());
                return resource;
            }
            throw new StorageFileNotFoundException("Could not read file: " + filename);
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename);
        }
    }
}

package com.trisysit.spring.files.upload.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.trisysit.spring.files.upload.message.ResponseMessage;
import com.trisysit.spring.files.upload.model.FileInfo;
import com.trisysit.spring.files.upload.service.FilesStorageService;
import com.trisysit.spring.files.util.AppUtil;

@Controller
@CrossOrigin("http://localhost:8080")
public class FilesController {

	@Autowired
	FilesStorageService storageService;

	@PostMapping("/upload")
	public ResponseEntity<List<FileInfo>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
		String message = "";
		try {
			List<String> fileNames = new ArrayList<>();
			ArrayList<LinkedHashMap<String, String>> map = new ArrayList<>();
			Arrays.asList(files).stream().forEach(file -> {
				storageService.save(file);
				fileNames.add(file.getOriginalFilename());
				Resource uploadFile = storageService.load(file.getOriginalFilename());
				try {
					URL attachmentPath = uploadFile.getURL();
					String attachmentPathStr = attachmentPath.toString().substring(5);
					LinkedHashMap<String, String> map1 = AppUtil.readDataLineByLine(attachmentPathStr,
							file.getOriginalFilename());
					map.add(map1);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			AppUtil.populateFilesData(map);
			List<FileInfo> fileInfos = new ArrayList<>();
			ArrayList<String> name = new ArrayList<>();
			name.add("success.csv");
			name.add("error.csv");
			for(String fileName : name) {
				String url = MvcUriComponentsBuilder
						.fromMethodName(FilesController.class, "getFile", fileName).build().toString();
				fileInfos.add(new FileInfo(fileName, url));
			}
			
			message = "Uploaded the files successfully: " + fileNames;
			return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
		} catch (Exception e) {
			message = "Fail to upload files!";
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
		}
	}

	@GetMapping("/files")
	public ResponseEntity<List<FileInfo>> getListFiles() {
		List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
			String filename = path.getFileName().toString();
			String url = MvcUriComponentsBuilder
					.fromMethodName(FilesController.class, "getFile", path.getFileName().toString()).build().toString();

			return new FileInfo(filename, url);
		}).collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
	}

	@GetMapping("/files/{filename:.+}")
	public ResponseEntity<Resource> getFile(@PathVariable String filename) {
		Resource file = storageService.load(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@DeleteMapping("/files/{filename:.+}")
	public ResponseEntity<ResponseMessage> deleteFile(@PathVariable String filename) {
		String message = "";

		try {
			boolean existed = storageService.delete(filename);

			if (existed) {
				message = "Delete the file successfully: " + filename;
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
			}

			message = "The file does not exist!";
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage(message));
		} catch (Exception e) {
			message = "Could not delete the file: " + filename + ". Error: " + e.getMessage();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
		}
	}
}

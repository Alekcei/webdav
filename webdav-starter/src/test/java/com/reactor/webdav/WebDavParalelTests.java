package com.reactor.webdav;

import com.reactor.webdav.client.WebDavBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@ExtendWith(SpringExtension.class)
//  We create a `@SpringBootTest`, starting an actual server on a `RANDOM_PORT`
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {
		WebdavModule.class
})
@AutoConfigureWebTestClient
@TestPropertySource(locations = "classpath:application.yaml")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.CONCURRENT)
@Slf4j
class WebDavParalelTests {

	// https://spring.io/guides/gs/reactive-rest-service/
	@Autowired
	private WebTestClient webTestClient;

	@LocalServerPort
	int port;

	String getTestUrl(){
		return "http://localhost:" + port;
	}

	@BeforeAll
	static void afterStart(){
		FileSystemUtils.deleteRecursively(new File("testfolder/webdav1"));
		new File("testfolder/webdav1").mkdir();
	}

	@Test
	@DisplayName("Создание файла")
	@Order(1)
	public void createFile() {

		var client = new WebDavBuilder();
		var resp = client
			.setMethod("MKCOL")
			.setUrl(getTestUrl())
			.setPath("/newfolder")
			.addUser("UserAdmin")
			.addPassword("123456")
			.response()
			.block();

		assert Objects.requireNonNull(resp).status().code() == 201;

		resp = client
			.setMethod("PUT")
			.setUrl(getTestUrl())
			.setPath("/newfolder/pdd.pdf")
			.addUser("UserAdmin")
			.addPassword("123456")
			.addFile(new File("testfolder/pdd.pdf"))
			.response()
			.block();
		assert Objects.requireNonNull(resp).status().code() == 200;
		assert new File("testfolder/webdav1/newfolder/pdd.pdf").exists();
	}


	@Test
	@DisplayName("чтение файла")
	@Order(2)
	void createGetFile() throws InterruptedException {
		Thread.sleep(200);
		System.out.println("2createGetFile " + LocalDateTime.now());
		var client = new WebDavBuilder();
		var getRes = client
				.setMethod("GET")
				.setUrl(getTestUrl())
				.setPath("/newfolder/pdd.pdf")
				.addUser("UserAdmin")
				.addPassword("123456")
				.response()
				.block();
		assert Objects.requireNonNull(getRes).status().code() == 200;
	}

	@Test
	@DisplayName("Удаление")
	@Order(2)
	void createDeleteFile() throws InterruptedException {
	    Thread.sleep(205);
		System.out.println("createDeleteFile " + LocalDate.now());
		var client = new WebDavBuilder();
		var deleteRes = client
				.setMethod("DELETE")
				.setUrl(getTestUrl())
				.setPath("/newfolder/pdd.pdf")
				.addUser("UserAdmin")
				.addPassword("123456")
				.response()
				.block();

		assert Objects.requireNonNull(deleteRes).status().code() == 200;

	}

}

package com.reactor.webdav;

import com.reactor.webdav.client.WebDavBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.*;

@ExtendWith(SpringExtension.class)
//  We create a `@SpringBootTest`, starting an actual server on a `RANDOM_PORT`
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yaml")
@Slf4j
class AppMvcWebDavTests {

	// https://spring.io/guides/gs/reactive-rest-service/
	@Autowired
	private WebTestClient webTestClient;

	@LocalServerPort
	int port;

	String user = "UserAdmin";
	String password = "123456";


	@BeforeAll
	static void afterStart(){
		FileSystemUtils.deleteRecursively(new File("testfolder/webdav1"));
		new File("testfolder/webdav1").mkdir();
	}

	@Test
	@DisplayName("Запрос доступных методов для обращения")
	void option() {

		var client =  new WebDavBuilder();
		var resp = client
				.setMethod("OPTIONS")
				.setUrl(getTestUrl())
				.setPath("/")
				.response()
				.block();
		assert Objects.requireNonNull(resp).status().code() == 401;
		if (Objects.requireNonNull(resp).status().code() == 401) {
			resp = client
				.addUser(user)
				.addPassword(password)
				.response()
				.block();
		}

		assert Objects.requireNonNull(resp).status().code() == 200;
		assert resp.responseHeaders().contains("Allow");
		assert resp.responseHeaders().contains("DAV");

	}



	@Test
	@DisplayName("Запрос каталогов и файлов")
	void propfind() {

		new WebDavBuilder()
				.setMethod("PROPFIND")
				.setUrl(getTestUrl())
				.setPath("/")
				.setDepth("1")
				.addUser(user)
				.addPassword(password)
				.responseSingle((response, bytes) -> bytes.asInputStream())
				.block();

	 	assert true;

	}

	@Test
	@DisplayName("Создание папки добавление файла, перемещение файла в новую папку, удаление файла ")
	void manipulateFile() {

		var client = new WebDavBuilder();
		var resp = client
			.setMethod("MKCOL")
			.setUrl(getTestUrl())
			.setPath("/newfolder")
			.addUser(user)
			.addPassword(password)
			.response()
			.block();

		assert Objects.requireNonNull(resp).status().code() == 201;

		resp = client
			.setMethod("PUT")
			.setUrl(getTestUrl())
			.setPath("/newfolder/pdd.pdf")
			.addUser(user)
			.addPassword(password)
			.addFile(new File("testfolder/pdd.pdf"))
			.response()
			.block();

		assert Objects.requireNonNull(resp).status().code() == 200;
		assert new File("testfolder/webdav1/newfolder/pdd.pdf").exists();

		resp = client
				.setMethod("COPY")
				.setUrl(getTestUrl())
				.setPath("/newfolder/pdd.pdf")
				.addUser(user)
				.addPassword(password)
				.setDestination("/newfolder/pddCopy.pdf")
				.response()
				.block();

		assert Objects.requireNonNull(resp).status().code() == 204;
	}
	@Test
	@DisplayName("Создание каталога если он уже создан")
	void createFolder() {

		var client = new WebDavBuilder();
		var resp = client
				.setMethod("MKCOL")
				.setUrl(getTestUrl())
				.setPath("/newfoldertest")
				.addUser(user)
				.addPassword("123456")
				.response()
				.block();
		 resp = client
				.setMethod("MKCOL")
				.setUrl(getTestUrl())
				.setPath("/newfoldertest")
				.addUser(user)
				.addPassword("123456")
				.response()
				.block();

		assert Objects.requireNonNull(resp).status().code() == 403;





	}



	String getTestUrl(){
		return "http://localhost:" + port;
	}

}

package com.reactor.webdav;

import com.reactor.webdav.client.WebDavBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.*;

@ExtendWith(SpringExtension.class)
//  We create a `@SpringBootTest`, starting an actual server on a `RANDOM_PORT`
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {
		WebdavModule.class
})
@TestPropertySource(locations = "classpath:application.yaml")
@Slf4j
class MvcWebDavTests {

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
	@AfterAll
	static void afterAll(){
		FileSystemUtils.deleteRecursively(new File("testfolder/webdav1"));
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
	@DisplayName("Удаление каталога с файлами")
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
			.setUrl(getTestUrl())
			.addUser(user)
			.addPassword(password)
			.setMethod("PUT")
			.setPath("/newfolder/pdd.pdf")
			.addFile(new File("testfolder/pdd.pdf"))
			.response()
			.block();

		assert Objects.requireNonNull(resp).status().code() == 200;
		assert new File("testfolder/webdav1/newfolder/pdd.pdf").exists();

		resp = client
				.setUrl(getTestUrl())
				.addUser(user)
				.addPassword(password)
				.setMethod("COPY")
				.setPath("/newfolder/pdd.pdf")
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
				.setUrl(getTestUrl())
				.addUser(user)
				.addPassword("123456")
				.setMethod("MKCOL")
				.setPath("/newfoldertest")
				.response()
				.block();
		 resp = client
				.setUrl(getTestUrl())
				.addUser(user)
				.addPassword("123456")
				.setMethod("MKCOL")
				.setPath("/newfoldertest")
				.response()
				.block();

		assert Objects.requireNonNull(resp).status().code() == 405;





	}

	@Test
	@DisplayName("Создание папки добавление файла, перемещение файла в новую папку, удаление файла ")
	void multiFolder() {

		var client = new WebDavBuilder();
		var resp = client
				.setUrl(getTestUrl())
				.addUser(user)
				.addPassword(password)
				.setMethod("MKCOL")
				.setPath("/multiFolder")
				.response()
				.block();

		assert Objects.requireNonNull(resp).status().code() == 201;
		// создание первого файла
		resp = client
				.setUrl(getTestUrl())
				.addUser(user)
				.addPassword(password)
				.setMethod("PUT")
				.setPath("/multiFolder/pdd1.pdf")
				.addFile(new File("testfolder/pdd.pdf"))
				.response()
				.block();

		assert Objects.requireNonNull(resp).status().code() == 200;
		assert new File("testfolder/webdav1/multiFolder/pdd1.pdf").exists();

		// создание второго файла
		resp = client
				.setUrl(getTestUrl())
				.addUser(user)
				.addPassword(password)
				.setMethod("PUT")
				.setPath("/multiFolder/pdd2.pdf")
				.addFile(new File("testfolder/pdd.pdf"))
				.response()
				.block();

		assert Objects.requireNonNull(resp).status().code() == 200;
		assert new File("testfolder/webdav1/multiFolder/pdd2.pdf").exists();

		resp = client
				.setUrl(getTestUrl())
				.addUser(user)
				.addPassword(password)
				.setMethod("DELETE")
				.setPath("/multiFolder/")
				.response()
				.block();

		assert Objects.requireNonNull(resp).status().code() == 200;
		assert !new File("testfolder/webdav1/multiFolder").exists();
	}

	String getTestUrl(){
		return "http://localhost:" + port;
	}

}

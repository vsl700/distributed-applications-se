package com.vsl700.nitflex;

import com.vsl700.nitflex.components.WebsiteCredentials;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NitflexApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void credentialsLoad(@Autowired WebsiteCredentials.Zamunda credentials){
		assertThat(credentials.getPassword()).isNotNull();
	}

}

package cz.istep.javatest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.istep.javatest.data.FrameworkVersion;
import cz.istep.javatest.data.HypeLevel;
import cz.istep.javatest.data.JavaScriptFramework;
import cz.istep.javatest.repository.FrameworkVersionRepository;
import cz.istep.javatest.repository.JavaScriptFrameworkRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class JavaScriptFrameworkTests {

	@Autowired
	private MockMvc mockMvc;
	
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private JavaScriptFrameworkRepository repository;
	@Autowired
	private FrameworkVersionRepository versionRepository;

	@Before
	public void prepareData() throws Exception {
		repository.deleteAll();
		versionRepository.deleteAll();

		JavaScriptFramework react = new JavaScriptFramework("React", HypeLevel.TRENDING);
		JavaScriptFramework vue = new JavaScriptFramework("Vue.js", HypeLevel.EMERGING);

		FrameworkVersion react_v1 = new FrameworkVersion("16.8.0", react);
		FrameworkVersion vue_v1 = new FrameworkVersion("2.6.0", vue);

		repository.save(react);
		repository.save(vue);
		versionRepository.save(react_v1);
		versionRepository.save(vue_v1);

	}

	@Test
	public void frameworksTest() throws Exception {
		prepareData();

		mockMvc.perform(get("/frameworks")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].name", is("React")))
				.andExpect(jsonPath("$[1].name", is("Vue.js")));
	}
	
	@Test
	@Transactional
	public void addFrameworkInvalid() throws Exception {
		JavaScriptFramework framework = new JavaScriptFramework();
		mockMvc.perform(post("/frameworks").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(framework)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field", is("name")))
				.andExpect(jsonPath("$.errors[0].message", is("NotEmpty")));
		
		framework.setName("verylongnameofthejavascriptframeworkjavaisthebest");
		mockMvc.perform(post("/frameworks").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(framework)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors", hasSize(1)))
			.andExpect(jsonPath("$.errors[0].field", is("name")))
			.andExpect(jsonPath("$.errors[0].message", is("Size")));
	}

	@Test
	public void testFrameworkCreation() throws Exception {
		JavaScriptFramework newFramework = new JavaScriptFramework("Angular", HypeLevel.PEAK);
		mockMvc.perform(post("/frameworks").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(newFramework)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Angular")))
				.andExpect(jsonPath("$.hypeLevel", is("PEAK")));
	}

	@Test
	@Transactional
	public void testFrameworkEdition() throws Exception {
		JavaScriptFramework existingFramework = repository.findByName("React");
		existingFramework.setHypeLevel(HypeLevel.DECLINING);
		mockMvc.perform(put("/frameworks/" + existingFramework.getId()).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(existingFramework)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("React")))
				.andExpect(jsonPath("$.hypeLevel", is("DECLINING")));
	}

	@Test
	public void testFrameworkDeletion() throws Exception {
		JavaScriptFramework existingFramework = repository.findByName("Vue.js");
		mockMvc.perform(delete("/frameworks/" + existingFramework.getId()))
				.andExpect(status().isOk());
	}

	@Test
	@Transactional
	public void testFrameworkVersionCreation() throws Exception {
		JavaScriptFramework existingFramework = repository.findByName("React");
		FrameworkVersion newVersion = new FrameworkVersion("17.0.0", existingFramework);

		mockMvc.perform(post("/frameworks/" + existingFramework.getId() + "/versions").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(newVersion)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.version", is("17.0.0")));
	}
}

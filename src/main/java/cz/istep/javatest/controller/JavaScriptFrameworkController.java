package cz.istep.javatest.controller;

import cz.istep.javatest.data.FrameworkVersion;
import cz.istep.javatest.data.JavaScriptFramework;
import cz.istep.javatest.repository.FrameworkVersionRepository;
import cz.istep.javatest.repository.JavaScriptFrameworkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class JavaScriptFrameworkController {

	private final JavaScriptFrameworkRepository repository;
	private final FrameworkVersionRepository versionRepository;

	@Autowired
	public JavaScriptFrameworkController(JavaScriptFrameworkRepository repository, FrameworkVersionRepository versionRepository) {
		this.repository = repository;
		this.versionRepository = versionRepository;
	}

	@GetMapping("/frameworks")
	public Iterable<JavaScriptFramework> frameworks() {
		return repository.findAll();
	}

	@PostMapping("/frameworks")
	public JavaScriptFramework createFramework(@Valid @RequestBody JavaScriptFramework newFramework) {
		return repository.save(newFramework);
	}

	@PutMapping("/frameworks/{id}")
	public JavaScriptFramework updateFramework(@RequestBody JavaScriptFramework newFramework, @PathVariable Long id) {
		return repository.findById(id)
				.map(framework -> {
					framework.setName(newFramework.getName());
					framework.setHypeLevel(newFramework.getHypeLevel());
					framework.setVersions(newFramework.getVersions());
					return repository.save(framework);
				})
				.orElseGet(() -> {
					newFramework.setId(id);
					return repository.save(newFramework);
				});
	}

	@DeleteMapping("/frameworks/{id}")
	public void deleteFramework(@PathVariable Long id) {
		repository.deleteById(id);
	}

	@PostMapping("/frameworks/{id}/versions")
	public FrameworkVersion createVersion(@RequestBody FrameworkVersion newVersion, @PathVariable Long id) {
		return repository.findById(id)
				.map(framework -> {
					newVersion.setFramework(framework);
					return versionRepository.save(newVersion);
				})
				.orElseThrow(() -> new RuntimeException("Framework not found"));
	}
}

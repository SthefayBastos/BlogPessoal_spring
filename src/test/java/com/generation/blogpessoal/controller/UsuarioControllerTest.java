package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.model.UsuarioLogin;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsuarioControllerTest {

	@Autowired
	private TestRestTemplate testRestTemplate;
	
	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@BeforeAll
	void start () {
		usuarioRepository.deleteAll();
		usuarioService.cadastrarUsuario(new Usuario(0L,
				"Root", "root@root.com.br", "rootroot", " "));
	}
	
	@Test
	@DisplayName("Cadastrar Um Usuário")
	public void deveCriarUmUsuario() {
		
		HttpEntity<Usuario>corpoRequisicao =  new HttpEntity <Usuario>(new Usuario (0L, 
				"Marcela Cabral", "marcela.@email.com.br", "12345678", " " ));
		ResponseEntity<Usuario>corpoResposta = testRestTemplate
				.exchange("/usuarios/cadastrar", HttpMethod.POST, corpoRequisicao, Usuario.class);
		assertEquals(HttpStatus.CREATED,corpoResposta.getStatusCode());
		
	}
	
	@Test
	@DisplayName("Não deve permitir duplicação do Usuário")
	public void naoDevePermitirDuplicarUsuario() {
		
		usuarioService.cadastrarUsuario(new Usuario(0L, 
				"Mavis Dracula", "mavis@email.com.br","12345678"," "));
		HttpEntity<Usuario>corpoRequisicao = new HttpEntity<Usuario>(new Usuario(0L,
				"Mavis Dracula", "mavis@email.com.br","123456789", " "));
		ResponseEntity<Usuario>corpoResposta = testRestTemplate
				.exchange("/usuarios/cadastrar", HttpMethod.POST, corpoRequisicao, Usuario.class);
		assertEquals(HttpStatus.BAD_REQUEST, corpoResposta.getStatusCode());
	}
	
	@Test
	@DisplayName("Atualizar um Usuário")
	public void deveAtualizarUmUsuario() {
		Optional<Usuario> usuarioCadastrado = usuarioService.cadastrarUsuario(new Usuario(0L,
				"Janiel Cardoso", "janiel@email.com.br", "12345678", " "));
		
		HttpEntity<Usuario> corpoRequisicao =  new HttpEntity <Usuario>(new Usuario(usuarioCadastrado.get().getId(),
				"Janiel Sousa Cardoso", "janielc@email.com.br", "12305678", " "));
		
		
		ResponseEntity<Usuario> corpoResposta = testRestTemplate
				.withBasicAuth("root@root.com", "rootroot")
				.exchange("/usuarios/atualizar", HttpMethod.PUT, corpoRequisicao, Usuario.class);
		assertEquals(HttpStatus.OK, corpoResposta.getStatusCode());
				
	}
	
	@Test
	@DisplayName("Listar todos os Usuários")
	public void deveMostrarTodosUsuarios() {
		
		usuarioService.cadastrarUsuario(new Usuario (0L,
				"Barbara Araujo", "barbara@email.com.br","12345678", " "));
		
		usuarioService.cadastrarUsuario(new Usuario (0L,
				"Benicio Alves", "benicio@email.com.br","12345678", " "));
		
		ResponseEntity<String> resposta =  testRestTemplate
				.withBasicAuth("root@root.com", "rootroot")
				.exchange("/usuarios/all", HttpMethod.GET, null, String.class);
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
	}
	@Test
	@DisplayName(" buscar por Id")
	public void buscarPorId() {

		/*
		 * 
		 * Cadastra um usuário para ter um ID | válido orElseThrow lança uma exceção se
		 * o Optional estiver vazio
		 */

		Usuario usuarioCadastrado = usuarioService
				.cadastrarUsuario(new Usuario(0L, "Mimi Sujiro", "msujiro@email.com", "4321mimi", "-"))
				.orElseThrow(() -> new IllegalStateException("Usuário não cadastrado"));

		/* Requisição HTTP para buscar o usuário por ID */
		ResponseEntity<Usuario> resposta = testRestTemplate.withBasicAuth("root@root.com", "rootroot")
				.exchange("/usuarios/{id}", HttpMethod.GET, null, Usuario.class, usuarioCadastrado.getId());

		/* Verifica o HTTP Status Code */
		assertEquals(HttpStatus.OK, resposta.getStatusCode());

		/* Verifica se o usuário retornado é o mesmo que foi cadastrado */
		assertEquals(usuarioCadastrado.getId(), resposta.getBody().getId());
		assertEquals(usuarioCadastrado.getNome(), resposta.getBody().getNome());

	}

	@Test
	@DisplayName("Usuario Login")
	public void login() {

		/* Cadastra um usuário para fins de teste */
		usuarioService.cadastrarUsuario(new Usuario(0L, "Maria Candela", "candela@email.com", "cand8765", "-"));

		/* Corpo da Requisição para o login */
		UsuarioLogin usuarioLogin = new UsuarioLogin(0L, "", "candela@email.com", "cand8765", "", "");
		ResponseEntity<UsuarioLogin> corpoResposta = testRestTemplate.exchange("/usuarios/logar", HttpMethod.POST,
				new HttpEntity<>(usuarioLogin), UsuarioLogin.class);

		/* Verifica o HTTP Status Code */
		assertEquals(HttpStatus.OK, corpoResposta.getStatusCode());

		/* Verificações */
		assertNotNull(corpoResposta.getBody(), "O corpo da resposta não deve ser nulo");
		
	}
	
}

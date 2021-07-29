package curso.api.rest.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import curso.api.rest.ApplicationContextLoad;
import curso.api.rest.model.Usuario;
import curso.api.rest.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Component
public class JWTTokenAutenticacaoService {
	
	/*Tempo de validade do Token 2 dias*/
	private static final long EXPIRATION_TIME = 172800000;
	
	/*Uma senha única para compor a autenticação e ajudar na segurança*/
	private static final String SECRET = "SenhaExtremamenteSecreta";

	/*Prefixo padrão de Token*/
	private static final String TOKEN_PREFIX = "Bearer";
	
	private static final String HEADER_STRING = "Authorization";
	
	public void addAuthentication(HttpServletResponse response, String username)throws IOException {
		
		/*Montagem do Token*/
		String JWT = Jwts.builder()/*Chama o gerador de Token*/
				        .setSubject(username)/*Adiciona o usuário*/
				        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))/*Tempo de expiração*/
				        .signWith(SignatureAlgorithm.HS512, SECRET).compact();/*Compactação e algoritmos de geração de senha*/
		/*Junta token com o prefixo*/
		String token = TOKEN_PREFIX + " " + JWT;/*Bearer fmfkho4rkg992nsm60fdf45shmb7d2d*/
		
		/*Adiciona no cabeçalho http*/
		response.addHeader(HEADER_STRING, token);/*Authorization:Bearer fmfkho4rkg992nsm60fdf45shmb7d2d*/
		
		/*Escreve token como resposta no corpo http*/
		response.getWriter().write("{\"Authorization\": \""+token+"\"}");
	}
	
	/*Retorna o usuário validado com token ou caso não seja válido retorna null*/
	public Authentication getAuthentication(HttpServletRequest request) {
		/*Pega o token enviado no cabeçalho*/
		String token = request.getHeader(HEADER_STRING);
		               
		if(token != null) {
			/*Faz a validação do token do usuário*/
			String user = Jwts.parser().setSigningKey(SECRET)/*Bearer fmfkho4rkg992nsm60fdf45shmb7d2d*/
			                  .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))/*fmfkho4rkg992nsm60fdf45shmb7d2d*/
			                  .getBody().getSubject();/*João Silva*/
		if(user != null) {
			
			Usuario usuario = ApplicationContextLoad.getApplicationContext()
					        .getBean(UsuarioRepository.class).findUserByLogin(user);
			/*Retorna o usuário logado*/
			if(usuario != null) {
				
				return new UsernamePasswordAuthenticationToken(
						usuario.getLogin(),
						usuario.getSenha(),
						usuario.getAuthorities());
				
			}
			
		}
		
	}
			return null; /*Não autorizado*/
		
	}
}

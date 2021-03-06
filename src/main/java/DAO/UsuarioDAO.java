package DAO;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import database.ConDB;
import entidade.Permissao;
import entidade.Retorno;
import entidade.Usuario;
import entidade.UsuarioCodigo;
import util.StringMD5;

public class UsuarioDAO {
	private Connection con;
	
	private StringMD5 md5;
	
	public UsuarioDAO() {
		con = ConDB.getConnection();
	}
	
	public String md5(String password) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(password.getBytes(),0,password.length());
			return new BigInteger(1,m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public boolean novaSenha(Usuario user) {
		if (con != null) {
			Calendar c = Calendar.getInstance();
			user.setSenha("polis" + c.getWeekYear());
			
			String SQL = "UPDATE usuario SET senha = ? WHERE codigo = ?";
			PreparedStatement ps;
			try {
				ps = con.prepareStatement(SQL);
				
				ps.setString(1, md5(user.getSenha()));
				ps.setInt(2, user.getCodigo());

				return ps.executeUpdate() > 0;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	public List<Usuario> listaUsuarios() {
		if (con != null) {

			List<Usuario> usuarios = new ArrayList<Usuario>();

			String SQL = "SELECT u.*, p.* FROM usuario as u, permissao as p WHERE u.codigo = p.codigo ORDER BY u.codigo";

			try {
				PreparedStatement ps = con.prepareStatement(SQL);
				ResultSet rs = ps.executeQuery();

				while (rs.next()) {
					Usuario u = new Usuario();
					u.setCodigo(rs.getInt("u.codigo"));
					u.setNome(rs.getString("u.nome"));
					u.setSobrenome(rs.getString("u.sobrenome"));
					u.setUsuario(rs.getString("u.usuario"));
					u.setEmail(rs.getString("u.email"));

					Permissao p = new Permissao();
					p.setCodigo(rs.getInt("p.codigo"));
					p.setCria_boleto(rs.getInt("p.cria_boleto"));
					p.setCria_usuario(rs.getInt("p.cria_usuario"));
					p.setDeleta_boleto(rs.getInt("p.deleta_boleto"));
					p.setDeleta_usuario(rs.getInt("p.deleta_usuario"));
					p.setEdita_boleto(rs.getInt("p.edita_boleto"));
					p.setEdita_usuario(rs.getInt("p.edita_usuario"));
					p.setStatu(rs.getInt("p.statu"));

					u.setPermissao(p);

					usuarios.add(u);
				}

				return usuarios;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public boolean excluirUsuario(Integer codigo) {
		if(con != null) {
			String SQL = "DELETE FROM permissao WHERE codigo = " + codigo;
			String SQL_U = "DELETE FROM usuario WHERE codigo = " + codigo;
			try {
				PreparedStatement ps = con.prepareStatement(SQL);
				if(ps.executeUpdate() > 0) {
					ps = con.prepareStatement(SQL_U);
					System.out.println("Apagando usu�rio");
					return ps.executeUpdate() > 0;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean mudaStatus(Usuario u) {
		if (con != null) {
			String SQL;
			if (u.getPermissao().getStatu() != 1) {
				SQL = "UPDATE permissao SET statu = 1 WHERE codigo = " + u.getCodigo();
			} else {
				SQL = "UPDATE permissao SET statu = 0 WHERE codigo = " + u.getCodigo();
			}

			try {
				PreparedStatement ps = con.prepareStatement(SQL);
				if (ps.executeUpdate() > 0) {
					return true;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public List<UsuarioCodigo> codigoUsuarios(){
		if(con != null) {
			List<UsuarioCodigo> codigos = listaUCodigo("SELECT * FROM usuario");
			return codigos;
		}
		return null;
	}
	
	private List<UsuarioCodigo> listaUCodigo(String SQL) {
		List<UsuarioCodigo> uCodigos = new ArrayList<UsuarioCodigo>();
		try {
			PreparedStatement ps = con.prepareStatement(SQL);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				UsuarioCodigo uCodigo = new UsuarioCodigo();
				uCodigo.setCodigo(rs.getInt("usuario.codigo"));
				uCodigos.add(uCodigo);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uCodigos; 
	}
	
	public Usuario buscaUsuarioID(int codigo) {
		if (con != null) {
			Usuario u = new Usuario();
			String SQL = "SELECT * FROM usuario WHERE codigo = " + codigo;

			try {
				PreparedStatement ps = con.prepareStatement(SQL);
				ResultSet rs = ps.executeQuery();

				if (rs.next()) {
					u.setCodigo(rs.getInt("codigo"));
					u.setEmail(rs.getString("email"));
					u.setNome(rs.getString("nome"));
					u.setSobrenome(rs.getString("sobrenome"));
					u.setUsuario(rs.getString("usuario"));
					
					return u;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	// *********** buscaUsuarioRetornaID
	// ********************************************
	// *********** Esse m�todo � o tipo retorno, onde retorna o ID do usuario,
	// ******
	// *********** e um true, para simboliza que deu tudo certo
	// *********************
	public Retorno buscaUsuarioRetornaID(Usuario u, String SQL) throws SQLException {
		if (con != null) {
			String SQL_BUSCA_ID = "SELECT codigo FROM usuario WHERE " + "usuario = " + "'" + u.getUsuario().toString()
					+ "'" + " AND " + "email = " + "'" + u.getEmail().toString() + "'" + ";";
			Integer indentificadorEncontrado = 0;

			// Prepara o SQL
			PreparedStatement ps = con.prepareStatement(SQL_BUSCA_ID);

			// Retorna as linhas do resultado do SQL_BUSCA_ID
			ResultSet rs = ps.executeQuery();

			// em quanto tiver tinhas no banco de dados, ele vai executar o while
			while (rs.next()) {
				// Retorna o codigo do usuario
				indentificadorEncontrado = rs.getInt("codigo");
			}
			System.out.println("ID ENCONTRADO : " + indentificadorEncontrado);

			// Insere o ID do usuario no codigo, criando uma linha na tabela permissao
			ps = con.prepareStatement(SQL + indentificadorEncontrado + ")");

			// Verifica se a inser��o deu certo
			if (ps.executeUpdate() > 0) {
				System.out.println("Permiss�es criadas com sucesso!");
				Retorno r = new Retorno("Permiss�es criadas com sucesso!", true);
				return r;
			} else {
				System.out.println("Permiss�es n�o foi criada!");
				Retorno r = new Retorno("Permiss�es n�o foi criada!", false);
				return r;
			}
		}

		return null;
	}

	// ******************************************************************************
	// ******************************************************************************
	// *********** cadastrarUsuario
	// *************************************************
	// *********** Esse m�todo � o tipo retorno, onde retorna o mensagem do
	// ocorrido*
	// *********** e um boolean
	// *****************************************************
	// ******************************************************************************
	public Retorno cadastrarUsuario(Usuario u) {
		if (con != null) {
			Retorno resul = new Retorno("", false);
			Retorno r = new Retorno();
			// prepara o SQL
			PreparedStatement ps;

			// ***************************************************************
			// *Codigo SQL a ser executado ***********************************
			// ***************************************************************
			String SQL_GERA_PERMISSAO = "INSERT INTO permissao(codigo) VALUE (";
			System.out.println(SQL_GERA_PERMISSAO);
			String SQL = "INSERT INTO usuario(usuario,nome,sobrenome,email,senha) VALUE(" + "'" + u.getUsuario() + "'"
					+ "," + "'" + u.getNome() + "'" + "," + "'" + u.getSobrenome() + "'" + "," + "'" + u.getEmail()
					+ "'" + "," + "'" + md5(u.getSenha()) + "'" + ")";
			System.out.println(SQL);
			try {
				// executa o sql de cria��o de usuario
				ps = con.prepareStatement(SQL);

				// verifica se a inser��o foi bem Sucedida
				if (ps.executeUpdate() > 0) {
					System.out.println("usuario cadastrado com sucesso!");
					try {

						// buscaUsuarioRetornaID, retorna ID do usuario criado
						r = buscaUsuarioRetornaID(u, SQL_GERA_PERMISSAO);
						System.out.println(r.getMensagem());
						resul.setRetorno(r.getRetorno());
						resul.setMensagem("Usuario inserido com sucesso! ");

						// case de problema com o buscaUsuarioRetornaID
					} catch (SQLException e) {
						e.printStackTrace();
						resul.setMensagem("Problema com " + r.getMensagem());
						System.out.println(resul.getMensagem());
						return resul;
					}

					// Retorno do TRY
					// Retorno do TRY
					return resul;

					// Se usuario j� existir no banco ou inser��o mau sucedida
				} else {
					System.out.println("Email/usuario j� existe no sistema!");
					resul.setMensagem("Email/usuario j� existe no sistema!");
					return resul;
				}
				// N�o conseguiu executar SQL, problema com a conex�o ou dados inserido
				// incorretamente!
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				//Verifica qual duplicidade �
				
				/*String fraseErr = e.toString();
				String busca[] = new String[2];
				
				busca[0] ="'email'";
				busca[1] ="'usuario'";
				
				for(int x = 0; x < busca[2].length();x++) {
					String confirma = fraseErr.toLowerCase();
					if(confirma.contains(busca[x])) {
						resul.setMensagem("Esse " + busca[x] + " J� existe no sistema");
						resul.setRetorno(false);
						System.out.println("Esse " + busca[x] + " J� existe no sistema");
						return resul;
					}
				}*/
				return resul;
			}
		}
		return null;
	} // Fecha metodo
	
	public Retorno buscaUsuarioLiberado(String codigo) {
		if (con != null) {
			String SQL_BUSCA_PERMISSAO = "SELECT * FROM permissao WHERE codigo =" + codigo;
			Retorno resultado = new Retorno();

			// Retorno PADR�O
			resultado.setRetorno(false);
			resultado.setPer(null);
			resultado.setMensagem("Problema com a conex�o");

			try {
				PreparedStatement ps = con.prepareStatement(SQL_BUSCA_PERMISSAO);
				ResultSet rs = ps.executeQuery();

				if (rs.next()) {
					Permissao per = new Permissao();

					per.setCria_boleto(rs.getInt("cria_boleto"));
					per.setCria_usuario(rs.getInt("cria_usuario"));
					per.setDeleta_boleto(rs.getInt("deleta_boleto"));
					per.setDeleta_usuario(rs.getInt("deleta_usuario"));
					per.setEdita_boleto(rs.getInt("edita_boleto"));
					per.setEdita_usuario(rs.getInt("edita_usuario"));
					per.setStatu(rs.getInt("statu"));

					resultado.setPer(per);

					if (resultado.getPer().getStatu() != 0) {
						resultado.setRetorno(true);
						return resultado;
					} else {
						resultado.setRetorno(false);
						return resultado;
					}
				} else {
					resultado.setMensagem("Nem um resultado encontrado!");
					return resultado;
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return resultado;
			}
		}
		return null;
	}
	
	public boolean modificarEmail(Usuario u, String email) {
		if(con != null) {
			String SQL = "UPDATE usuario SET email = ? WHERE codigo = ?";
			try {
				PreparedStatement ps = con.prepareStatement(SQL);
				ps.setString(1, email);
				ps.setInt(2, u.getCodigo());
				return ps.executeUpdate() > 0;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	// ******************************************************************************
	// ******************************************************************************
	// *********** cadastrarUsuario
	// *************************************************
	public Retorno loginUsuario(String usuario, String senha) {
		System.out.println("Senha digitada" + senha);
		if (con != null) {
			Retorno resultado = new Retorno("Usuario n�o encontrado", false);

			// Informa��es do usuario a ser retornado
			Usuario u = new Usuario();

			// String SQL a ser executada
			String SQL_BUSCA = "SELECT * FROM usuario WHERE usuario = " + "'" + usuario + "'" + "AND" + " senha = "
					+ "'" + senha + "'";

			// Prepara o SQL
			PreparedStatement ps;

			try {
				ps = con.prepareStatement(SQL_BUSCA);

				// Resultado da busca
				ResultSet rs = ps.executeQuery();

				// em quanto tiver tinhas no banco de dados, ele vai executar o while
				while (rs.next()) {
					u.setCodigo(rs.getInt("codigo"));
					u.setNome(rs.getString("nome"));
					u.setSobrenome(rs.getString("sobrenome"));
					u.setUsuario(rs.getString("usuario"));
					u.setEmail(rs.getString("email"));
					u.setSenha(rs.getString("senha"));
					
					resultado = buscaUsuarioLiberado(u.getCodigo().toString());

					if (resultado.getRetorno()) {
						// Retorno final
						resultado.setUser(u);
						resultado.setRetorno(true);
						resultado.setMensagem("Bem vindo " + u.getNome() + " " + u.getSobrenome());

						return resultado;
					} else {
						// Retorno final
						resultado.setUser(null);
						resultado.setRetorno(false);
						resultado.setMensagem("Usuario pentende no sistema");

						return resultado;
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return resultado;
		}
		return null;
	}
}

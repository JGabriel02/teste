import './App.css';
import imgagemEscolhida from './imagens/mercador.png'

function BotaoLogin() {
  return (
    <button className = "botao">
      Login
    </button>
  );
}
function BotaoCadastrar() {
  return(
    <button className = "botao">
      Cadastrar
    </button>
  )
}

function MyApp() {
  return (
    <div className="componentes">
      <h1>Bem vindo Estranho</h1>
      <div>
        <BotaoLogin />
        <BotaoCadastrar />
      </div>
      <img src={imgagemEscolhida} alt = "" className = "imagem"/>
    </div>
  );
}


export default MyApp;



package br.com.alura.estoque.repository;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProdutoRepository {
    private final ProdutoDAO dao;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
    }

    public void buscaProdutos(ProdutosCarregadosListener listener) {
        buscaProdutosInternos(listener);
    }

    private void buscaProdutosInternos(ProdutosCarregadosListener listener) {
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                  //notifica que o dado está pronto
                    listener.quandoCarregados(resultado);
                    buscaProdutosNaApi(listener);
                }).execute();
    }

    private void buscaProdutosNaApi(ProdutosCarregadosListener listener) {
        ProdutoService service = new EstoqueRetrofit().getProdutoService();
        Call<List<Produto>> call = service.buscaTodos();
        call.enqueue(new Callback<List<Produto>>() {
            private List<Produto> produtoList;

            @Override
            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
                produtoList = response.body();
                new BaseAsyncTask<>(() -> {
                    dao.salva(produtoList);
                    return dao.buscaTodos();
                }, resposta -> {
                    //notidica que o dado está pronto
                    listener.quandoCarregados(resposta);
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            @Override
            public void onFailure(Call<List<Produto>> call, Throwable t) {
                Log.i("Erro api", "onFailure: "+ t.getMessage());
            }
        });
    }

    public interface ProdutosCarregadosListener{
        void quandoCarregados(List<Produto> produtos);
    }

}

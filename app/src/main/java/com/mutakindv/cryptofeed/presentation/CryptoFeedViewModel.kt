package com.mutakindv.cryptofeed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutakindv.cryptofeed.domain.BadRequest
import com.mutakindv.cryptofeed.domain.Connectivity
import com.mutakindv.cryptofeed.domain.InternalServerError
import com.mutakindv.cryptofeed.domain.InvalidData
import com.mutakindv.cryptofeed.domain.NotFound
import com.mutakindv.cryptofeed.domain.CryptoFeed
import com.mutakindv.cryptofeed.domain.LoadCryptoFeedResult
import com.mutakindv.cryptofeed.domain.LoadCryptoFeedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class UiState(
    val isLoading: Boolean = false,
    val cryptoFeed: List<CryptoFeed> = emptyList(),
    val failed: String = "",
)

class CryptoFeedViewModel(val useCase: LoadCryptoFeedUseCase): ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(isLoading = true)
            }

            useCase.load().collect { result ->
                _uiState.update {
                    when(result) {
                        is LoadCryptoFeedResult.Success -> {
                            it.copy(isLoading = false, cryptoFeed = result.cryptoFeed)
                        }
                        is LoadCryptoFeedResult.Error -> {
                            it.copy(
                                isLoading = false,
                                failed = when(result.exception) {
                                    is Connectivity -> "Tidak ada internet"
                                    is InvalidData -> "Terjadi kesalahan"
                                    is BadRequest -> "Permintaan gagal, coba lagi"
                                    is NotFound -> "Tidak ditemukan, coba lagi"
                                    is InternalServerError -> "Server sedang dalam perbaikan, coba lagi"
                                    else -> "Terjadi kesalahan"
                                })
                        }

                    }
                }
            }
        }
    }

}


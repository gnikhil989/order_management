package com.example.order_management.service;

import com.example.order_management.dto.request.DepositRequest;
import com.example.order_management.dto.request.TransferRequest;
import com.example.order_management.dto.request.WithdrawRequest;
import com.example.order_management.dto.response.WalletResponse;
import com.example.order_management.dto.response.WalletTransactionResponse;
import com.example.order_management.entity.TransactionStatus;
import com.example.order_management.entity.TransactionType;
import com.example.order_management.entity.Wallet;
import com.example.order_management.entity.WalletTransaction;
import com.example.order_management.exception.InsufficientBalanceException;
import com.example.order_management.repository.WalletRepository;
import com.example.order_management.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WalletService.
 * Validates financial balance modifications, pessimistic locking logic,
 * P2P transfers, and immutable transaction ledger creation using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private Long userId;
    private Long walletId;
    private Wallet sampleWallet;

    @BeforeEach
    void setUp() {
        userId = 1L;
        walletId = 1L;

        sampleWallet = Wallet.builder()
                .id(walletId)
                .userId(userId)
                .balance(new BigDecimal("100.00"))
                .currency("INR")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }


    @Test
    @DisplayName("Should return existing wallet when getOrCreateWallet is called")
    void getOrCreateWallet_ExistingWallet_ReturnsWallet() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(sampleWallet));

        WalletResponse response = walletService.getOrCreateWallet(userId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(walletId);
        assertThat(response.balance()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Should create default wallet if none exists during getOrCreateWallet")
    void getOrCreateWallet_NewUser_CreatesDefaultWallet() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(sampleWallet);

        WalletResponse response = walletService.getOrCreateWallet(userId);

        assertThat(response).isNotNull();
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should successfully deposit funds, update balance, and record ledger entry")
    void deposit_Success_CreditsBalanceAndSavesLedger() {
        DepositRequest request = new DepositRequest(new BigDecimal("50.00"), "Paycheck top-up");

        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(sampleWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = walletService.deposit(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.balance()).isEqualByComparingTo("150.00");

        // Verify transaction ledger was recorded
        ArgumentCaptor<WalletTransaction> txCaptor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository).save(txCaptor.capture());

        WalletTransaction savedTx = txCaptor.getValue();
        assertThat(savedTx.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(savedTx.getAmount()).isEqualByComparingTo("50.00");
        assertThat(savedTx.getBeforeBalance()).isEqualByComparingTo("100.00");
        assertThat(savedTx.getAfterBalance()).isEqualByComparingTo("150.00");
        assertThat(savedTx.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
    }

    @Test
    @DisplayName("Should successfully withdraw funds when balance is sufficient")
    void withdraw_Success_DebitsBalanceAndSavesLedger() {
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("40.00"), "ATM withdrawal");

        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(sampleWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = walletService.withdraw(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.balance()).isEqualByComparingTo("60.00");

        // Verify transaction ledger
        ArgumentCaptor<WalletTransaction> txCaptor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository).save(txCaptor.capture());

        WalletTransaction savedTx = txCaptor.getValue();
        assertThat(savedTx.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(savedTx.getAmount()).isEqualByComparingTo("40.00");
        assertThat(savedTx.getBeforeBalance()).isEqualByComparingTo("100.00");
        assertThat(savedTx.getAfterBalance()).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException when withdrawal amount exceeds balance")
    void withdraw_InsufficientBalance_ThrowsException() {
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("200.00"), "Large withdrawal");

        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(sampleWallet));

        assertThatThrownBy(() -> walletService.withdraw(userId, request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    @DisplayName("Should successfully transfer funds between two wallets and create two ledger records")
    void transfer_Success_UpdatesBothWalletsAndSavesTwoLedgers() {
        Long recipientUserId = 2L;
        Long recipientWalletId = 2L;

        Wallet recipientWallet = Wallet.builder()
                .id(recipientWalletId)
                .userId(recipientUserId)
                .balance(new BigDecimal("20.00"))
                .currency("INR")
                .build();


        TransferRequest request = new TransferRequest(recipientUserId, new BigDecimal("30.00"), "Lunch split");

        Long firstId = userId < recipientUserId ? userId : recipientUserId;
        Long secondId = userId < recipientUserId ? recipientUserId : userId;

        when(walletRepository.findByUserIdWithLock(firstId))
                .thenReturn(Optional.of(userId.equals(firstId) ? sampleWallet : recipientWallet));
        when(walletRepository.findByUserIdWithLock(secondId))
                .thenReturn(Optional.of(userId.equals(secondId) ? sampleWallet : recipientWallet));

        WalletResponse response = walletService.transfer(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.balance()).isEqualByComparingTo("70.00");
        assertThat(recipientWallet.getBalance()).isEqualByComparingTo("50.00");

        // Verify 2 ledger records created (TRANSFER_OUT and TRANSFER_IN)
        verify(transactionRepository, times(2)).save(any(WalletTransaction.class));
        verify(walletRepository, times(2)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when attempting to transfer to own wallet")
    void transfer_SelfTransfer_ThrowsException() {
        TransferRequest request = new TransferRequest(userId, new BigDecimal("10.00"), "Self transfer");

        assertThatThrownBy(() -> walletService.transfer(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot transfer funds to your own wallet");
    }

    @Test
    @DisplayName("Should return transaction history list")
    void getTransactionHistory_Success_ReturnsList() {
        WalletTransaction tx = WalletTransaction.builder()
                .id(1L)
                .walletId(walletId)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .beforeBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                .afterBalance(new BigDecimal("100.00"))
                .status(TransactionStatus.SUCCESS)
                .description("Initial Deposit")
                .createdAt(LocalDateTime.now())
                .build();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(sampleWallet));
        when(transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId)).thenReturn(List.of(tx));

        List<WalletTransactionResponse> history = walletService.getTransactionHistory(userId);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(history.get(0).amount()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Should return passbook statement with summarized totals and paginated transactions")
    void getPassbook_Success_ReturnsPaginatedSummary() {
        WalletTransaction txDeposit = WalletTransaction.builder()
                .id(1L)
                .walletId(walletId)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .beforeBalance(BigDecimal.ZERO)
                .afterBalance(new BigDecimal("100.00"))
                .status(TransactionStatus.SUCCESS)
                .description("Top-up")
                .createdAt(LocalDateTime.now())
                .build();

        WalletTransaction txWithdraw = WalletTransaction.builder()
                .id(2L)
                .walletId(walletId)
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("30.00"))
                .beforeBalance(new BigDecimal("100.00"))
                .afterBalance(new BigDecimal("70.00"))
                .status(TransactionStatus.SUCCESS)
                .description("Withdrawal")
                .createdAt(LocalDateTime.now())
                .build();

        List<WalletTransaction> allTxList = List.of(txWithdraw, txDeposit);
        org.springframework.data.domain.Page<WalletTransaction> page = new org.springframework.data.domain.PageImpl<>(
                allTxList,
                org.springframework.data.domain.PageRequest.of(0, 10),
                2
        );

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(sampleWallet));
        when(transactionRepository.findByWalletId(any(Long.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);
        when(transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId)).thenReturn(allTxList);

        com.example.order_management.dto.response.PassbookResponse passbook = walletService.getPassbook(userId, 0, 10, null);

        assertThat(passbook).isNotNull();
        assertThat(passbook.totalDeposits()).isEqualByComparingTo("100.00");
        assertThat(passbook.totalWithdrawals()).isEqualByComparingTo("30.00");
        assertThat(passbook.totalTransactions()).isEqualTo(2);
        assertThat(passbook.transactions()).hasSize(2);
    }
}


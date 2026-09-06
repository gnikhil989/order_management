package com.example.order_management.controller;

import com.example.order_management.config.JwtAuthFilter;
import com.example.order_management.dto.request.DepositRequest;
import com.example.order_management.dto.request.TransferRequest;
import com.example.order_management.dto.request.WithdrawRequest;
import com.example.order_management.dto.response.PassbookResponse;
import com.example.order_management.dto.response.WalletResponse;
import com.example.order_management.dto.response.WalletTransactionResponse;
import com.example.order_management.entity.Role;
import com.example.order_management.entity.TransactionStatus;
import com.example.order_management.entity.TransactionType;
import com.example.order_management.entity.User;
import com.example.order_management.repository.UserRepository;
import com.example.order_management.service.CustomUserDetailsService;
import com.example.order_management.service.JwtService;
import com.example.order_management.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller slice unit tests for WalletController.
 * Validates HTTP routing, JSON serialization, and @Valid bean validation constraints.
 */
@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Long authenticatedUserId;
    private User authenticatedUser;
    private WalletResponse authenticatedUserWalletResponse;

    @BeforeEach
    void setUp() {
        authenticatedUserId = 1L;
        authenticatedUser = User.builder()
                .id(authenticatedUserId)
                .name("Nikhil")
                .email("nikhil@example.com")
                .password("hash")
                .role(Role.USER)
                .build();

        authenticatedUserWalletResponse = new WalletResponse(
                1L,
                authenticatedUserId,
                new BigDecimal("100.00"),
                "INR",
                LocalDateTime.now()
        );

        when(userRepository.findByEmail("nikhil@example.com")).thenReturn(Optional.of(authenticatedUser));
    }

    @Test
    @WithMockUser(username = "nikhil@example.com")
    @DisplayName("GET /api/v1/wallet - Should return 200 OK with wallet details")
    void getWallet_AuthenticatedUser_Returns200() throws Exception {
        when(walletService.getOrCreateWallet(authenticatedUserId)).thenReturn(authenticatedUserWalletResponse);

        mockMvc.perform(get("/api/v1/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(authenticatedUserId))
                .andExpect(jsonPath("$.balance").value(100.00))
                .andExpect(jsonPath("$.currency").value("INR"));
    }

    @Test
    @WithMockUser(username = "nikhil@example.com")
    @DisplayName("POST /api/v1/wallet/deposit - Should return 200 OK on valid deposit")
    void deposit_ValidAmount_Returns200() throws Exception {
        DepositRequest depositRequest = new DepositRequest(new BigDecimal("50.00"), "Top-up");
        WalletResponse expectedUpdatedWalletResponse = new WalletResponse(
                authenticatedUserWalletResponse.id(),
                authenticatedUserId,
                new BigDecimal("150.00"),
                "INR",
                LocalDateTime.now()
        );

        when(walletService.deposit(eq(authenticatedUserId), any(DepositRequest.class))).thenReturn(expectedUpdatedWalletResponse);

        mockMvc.perform(post("/api/v1/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.00));
    }

    @Test
    @WithMockUser(username = "nikhil@example.com")
    @DisplayName("POST /api/v1/wallet/deposit - Should return 400 Bad Request on negative or zero amount")
    void deposit_InvalidAmount_Returns400() throws Exception {
        DepositRequest invalidDepositRequest = new DepositRequest(new BigDecimal("0.00"), "Invalid deposit");

        mockMvc.perform(post("/api/v1/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDepositRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "nikhil@example.com")
    @DisplayName("POST /api/v1/wallet/withdraw - Should return 200 OK on valid withdrawal")
    void withdraw_ValidAmount_Returns200() throws Exception {
        WithdrawRequest withdrawRequest = new WithdrawRequest(new BigDecimal("30.00"), "Withdrawal");
        WalletResponse expectedUpdatedWalletResponse = new WalletResponse(
                authenticatedUserWalletResponse.id(),
                authenticatedUserId,
                new BigDecimal("70.00"),
                "INR",
                LocalDateTime.now()
        );

        when(walletService.withdraw(eq(authenticatedUserId), any(WithdrawRequest.class))).thenReturn(expectedUpdatedWalletResponse);

        mockMvc.perform(post("/api/v1/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(70.00));
    }

    @Test
    @WithMockUser(username = "nikhil@example.com")
    @DisplayName("POST /api/v1/wallet/transfer - Should return 200 OK on valid P2P transfer")
    void transfer_ValidPayload_Returns200() throws Exception {
        Long recipientUserId = 2L;
        TransferRequest transferRequest = new TransferRequest(recipientUserId, new BigDecimal("25.00"), "Gift");
        WalletResponse expectedUpdatedWalletResponse = new WalletResponse(
                authenticatedUserWalletResponse.id(),
                authenticatedUserId,
                new BigDecimal("75.00"),
                "INR",
                LocalDateTime.now()
        );

        when(walletService.transfer(eq(authenticatedUserId), any(TransferRequest.class))).thenReturn(expectedUpdatedWalletResponse);

        mockMvc.perform(post("/api/v1/wallet/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(75.00));
    }

    @Test
    @WithMockUser(username = "nikhil@example.com")
    @DisplayName("GET /api/v1/wallet/transactions - Should return 200 OK with transaction list")
    void getTransactionHistory_AuthenticatedUser_Returns200() throws Exception {
        WalletTransactionResponse mockDepositTransactionResponse = new WalletTransactionResponse(
                1L,
                authenticatedUserWalletResponse.id(),
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                TransactionStatus.SUCCESS,
                "Initial deposit",
                "DEP-12345",
                LocalDateTime.now()
        );

        when(walletService.getTransactionHistory(authenticatedUserId)).thenReturn(List.of(mockDepositTransactionResponse));

        mockMvc.perform(get("/api/v1/wallet/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].referenceId").value("DEP-12345"));
    }

    @Test
    @WithMockUser(username = "nikhil@example.com")
    @DisplayName("GET /api/v1/wallet/passbook - Should return 200 OK with passbook summary and paginated transactions")
    void getPassbook_AuthenticatedUser_Returns200() throws Exception {
        PassbookResponse mockPassbookResponse = new PassbookResponse(
                authenticatedUserWalletResponse.id(),
                new BigDecimal("100.00"),
                "INR",
                new BigDecimal("150.00"),
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1,
                0,
                1,
                10,
                List.of()
        );

        when(walletService.getPassbook(eq(authenticatedUserId), eq(0), eq(10), any())).thenReturn(mockPassbookResponse);

        mockMvc.perform(get("/api/v1/wallet/passbook?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(100.00))
                .andExpect(jsonPath("$.totalDeposits").value(150.00))
                .andExpect(jsonPath("$.totalWithdrawals").value(50.00))
                .andExpect(jsonPath("$.totalTransactions").value(1));
    }
}

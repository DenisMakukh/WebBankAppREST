package com.example.demo;

import com.example.demo.DTOs.AccountDTO;
import com.example.demo.DTOs.TransactionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationIT {

	@Autowired
	private MockMvc mockMvc;

	AccountDTO createAccount(double balance) throws Exception {
		String json =
				mockMvc.perform(post("/createAccount?balance=" + balance + "&password=password")
								.with(user("admin").password("password").roles("ADMIN"))
						)
						.andExpect(status().is2xxSuccessful())
						.andReturn().getResponse().getContentAsString();
		return new ObjectMapper().readValue(json, AccountDTO.class);
	}

	AccountDTO getAccount(long id) throws Exception {
		String json = mockMvc.perform(get("/getAccount?id=" + id)
						.with(user(String.valueOf(id)).password("password"))
				)
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();

		return new ObjectMapper().readValue(json, AccountDTO.class);
	}

	TransactionDTO createTransaction(long sender, long receiver, double money) throws Exception {
		String json = mockMvc.perform(post(
						"/createTransaction"
								+ "?receiver="
								+ receiver
								+ "&money="
								+ money
				)
						.with(user(String.valueOf(sender)).password("password")))
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();

		return new ObjectMapper().readValue(json, TransactionDTO.class);
	}

	@Test
	void testCreateAccountByAdmin() throws Exception {
		AccountDTO account = createAccount(1000);
		assertEquals(1000, account.getBalance());
		assertEquals(new HashMap<>(), account.getTransactions());
	}

	@Test
	void testCreateAccountByUser() throws Exception {
		AccountDTO account = createAccount(1000);

		mockMvc.perform(post("/createAccount?balance=2000&password=password")
						.with(user(String.valueOf(account.getId())).password("password"))
				)
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testGetAccountByUser() throws Exception {
		AccountDTO account = createAccount(1000);

		AccountDTO response = getAccount(account.getId());

		assertEquals(account.getTransactions(), response.getTransactions());
		assertEquals(account.getId(), response.getId());
		assertEquals(account.getBalance(), response.getBalance());
	}

	@Test
	void testGetAccountByWrongUser() throws Exception {
		AccountDTO account1 = createAccount(1000);
		AccountDTO account2 = createAccount(1000);

		mockMvc.perform(get("/getAccount?id=" + account1.getId())
						.with(user(String.valueOf(account2.getId())).password("password"))
				)
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testGetAccountByAdmin() throws Exception {
		AccountDTO account = createAccount(1000);

		String json = mockMvc.perform(get("/getAccount?id=" + account.getId())
						.with(user("admin").password("password").roles("ADMIN"))
				)
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();

		AccountDTO response = new ObjectMapper().readValue(json, AccountDTO.class);

		assertEquals(account.getTransactions(), response.getTransactions());
		assertEquals(account.getId(), response.getId());
		assertEquals(account.getBalance(), response.getBalance());
	}

	@Test
	void testCreateTransactionByUser() throws Exception {
		AccountDTO account1 = createAccount(1000);
		AccountDTO account2 = createAccount(2000);

		TransactionDTO transaction = createTransaction(account1.getId(), account2.getId(), 300);

		assertEquals(account1.getId(), transaction.getSender());
		assertEquals(account2.getId(), transaction.getReceiver());
		assertEquals(300, transaction.getMoney());
		assertFalse(transaction.isCancelled());

		AccountDTO newAccount1 = getAccount(account1.getId());
		AccountDTO newAccount2 = getAccount(account2.getId());

		assertEquals(700, newAccount1.getBalance());
		assertEquals(2300, newAccount2.getBalance());

		assertTrue(newAccount1.getTransactions().containsKey(transaction.getId()));
		assertTrue(newAccount2.getTransactions().containsKey(transaction.getId()));
	}

	@Test
	void testCancelTransactionBySender() throws Exception {
		AccountDTO account1 = createAccount(1000);
		AccountDTO account2 = createAccount(1000);

		TransactionDTO transaction = createTransaction(account1.getId(), account2.getId(), 300);

		mockMvc.perform(post("/cancelTransaction?id=" + transaction.getId())
				.with(user(String.valueOf(account1.getId())).password("password")))
				.andExpect(status().is2xxSuccessful());

		AccountDTO newAccount1 = getAccount(account1.getId());
		AccountDTO newAccount2 = getAccount(account2.getId());

		assertEquals(account1.getBalance(), newAccount1.getBalance());
		assertEquals(account2.getBalance(), newAccount2.getBalance());

		assertTrue(newAccount1.getTransactions().containsKey(transaction.getId()));
		assertTrue(newAccount2.getTransactions().containsKey(transaction.getId()));

		assertTrue(newAccount1.getTransactions().get(transaction.getId()).isCancelled());
		assertTrue(newAccount2.getTransactions().get(transaction.getId()).isCancelled());
	}

	@Test
	void testCancelTransactionByReceiver() throws Exception {
		AccountDTO account1 = createAccount(1000);
		AccountDTO account2 = createAccount(1000);

		TransactionDTO transaction = createTransaction(account1.getId(), account2.getId(), 300);

		mockMvc.perform(post("/cancelTransaction?id=" + transaction.getId())
						.with(user(String.valueOf(account2.getId())).password("password")))
				.andExpect(status().is2xxSuccessful());

		AccountDTO newAccount1 = getAccount(account1.getId());
		AccountDTO newAccount2 = getAccount(account2.getId());

		assertEquals(account1.getBalance(), newAccount1.getBalance());
		assertEquals(account2.getBalance(), newAccount2.getBalance());

		assertTrue(newAccount1.getTransactions().containsKey(transaction.getId()));
		assertTrue(newAccount2.getTransactions().containsKey(transaction.getId()));

		assertTrue(newAccount1.getTransactions().get(transaction.getId()).isCancelled());
		assertTrue(newAccount2.getTransactions().get(transaction.getId()).isCancelled());
	}

	@Test
	void testCancelTransactionByAdmin() throws Exception {
		AccountDTO account1 = createAccount(1000);
		AccountDTO account2 = createAccount(1000);

		TransactionDTO transaction = createTransaction(account1.getId(), account2.getId(), 300);

		mockMvc.perform(post("/cancelTransaction?id=" + transaction.getId())
						.with(user("admin").password("password").roles("ADMIN")))
				.andExpect(status().is2xxSuccessful());

		AccountDTO newAccount1 = getAccount(account1.getId());
		AccountDTO newAccount2 = getAccount(account2.getId());

		assertEquals(account1.getBalance(), newAccount1.getBalance());
		assertEquals(account2.getBalance(), newAccount2.getBalance());

		assertTrue(newAccount1.getTransactions().containsKey(transaction.getId()));
		assertTrue(newAccount2.getTransactions().containsKey(transaction.getId()));

		assertTrue(newAccount1.getTransactions().get(transaction.getId()).isCancelled());
		assertTrue(newAccount2.getTransactions().get(transaction.getId()).isCancelled());
	}

	@Test
	void testCancelTransactionByWrongUser() throws Exception {
		AccountDTO account1 = createAccount(1000);
		AccountDTO account2 = createAccount(1000);
		AccountDTO account3 = createAccount(1000);

		TransactionDTO transaction = createTransaction(account1.getId(), account2.getId(), 300);

		mockMvc.perform(post("/cancelTransaction?id=" + transaction.getId())
						.with(user(String.valueOf(account3.getId())).password("password")))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testCancelTransactionDoesWrongBalance() throws Exception {
		AccountDTO account1 = createAccount(1000);
		AccountDTO account2 = createAccount(1000);
		AccountDTO account3 = createAccount(1000);

		TransactionDTO transaction1 = createTransaction(account1.getId(), account2.getId(), 300);
		createTransaction(account2.getId(), account3.getId(), 1300);

		mockMvc.perform(post("/cancelTransaction?id=" + transaction1.getId())
						.with(user("admin").password("password").roles("ADMIN")))
				.andExpect(status().is4xxClientError());
	}
}

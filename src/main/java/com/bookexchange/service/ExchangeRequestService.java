package com.bookexchange.service;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.ExchangeRequest;
import com.bookexchange.entity.User;
import com.bookexchange.exception.BookNotFoundException;

import com.bookexchange.exception.UnauthorizedActionException;
import com.bookexchange.repository.BookRepository;
import com.bookexchange.repository.ExchangeRequestRepository;
import com.bookexchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ExchangeRequestService {

    @Autowired
    private ExchangeRequestRepository exchangeRequestRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ExchangeRequest> getReceivedRequests(String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return exchangeRequestRepository.findByOwnerOrderByCreatedAtDesc(owner);
    }

    public List<ExchangeRequest> getSentRequests(String email) {
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return exchangeRequestRepository.findByRequesterOrderByCreatedAtDesc(requester);
    }

    public ExchangeRequest createExchangeRequest(String email, Long requestedBookId, Long offeredBookId, String message) {
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Book requestedBook = bookRepository.findById(requestedBookId)
                .orElseThrow(() -> new BookNotFoundException("Requested book not found"));
        Book offeredBook = bookRepository.findById(offeredBookId)
                .orElseThrow(() -> new BookNotFoundException("Offered book not found"));

        if (requestedBook.getSeller().getId().equals(requester.getId())) {
            throw new UnauthorizedActionException("Cannot request exchange for your own book");
        }

        ExchangeRequest request = new ExchangeRequest();
        request.setRequester(requester);
        request.setOwner(requestedBook.getSeller());
        request.setRequestedBook(requestedBook);
        request.setOfferedBook(offeredBook);
        request.setMessage(message);

        return exchangeRequestRepository.save(request);
    }

    public ExchangeRequest acceptExchangeRequest(String email, Long id) {
        ExchangeRequest request = exchangeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exchange request not found"));

        if (!request.getOwner().getEmail().equals(email)) {
            throw new UnauthorizedActionException("You are not authorized to accept this request");
        }

        request.setStatus(ExchangeRequest.ExchangeStatus.ACCEPTED);
        return exchangeRequestRepository.save(request);
    }

    public ExchangeRequest declineExchangeRequest(String email, Long id) {
        ExchangeRequest request = exchangeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exchange request not found"));

        if (!request.getOwner().getEmail().equals(email)) {
            throw new UnauthorizedActionException("You are not authorized to decline this request");
        }

        request.setStatus(ExchangeRequest.ExchangeStatus.DECLINED);
        return exchangeRequestRepository.save(request);
    }

    public void cancelExchangeRequest(String email, Long id) {
        ExchangeRequest request = exchangeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exchange request not found"));

        if (!request.getRequester().getEmail().equals(email)) {
            throw new UnauthorizedActionException("You are not authorized to cancel this request");
        }

        exchangeRequestRepository.delete(request);
    }
}

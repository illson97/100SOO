package spring.soo.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class PaginationService {

    private static final int BAR_LENGTH = 5;

    public List<Integer> getPaginationBarNumbers(int currentPageNumber, int totalPages) {
        int starNumber = Math.max(currentPageNumber - (BAR_LENGTH / 2), 0);
        int endNumber = Math.min(starNumber + BAR_LENGTH, totalPages);

        return IntStream.range(starNumber, endNumber).boxed().toList();
    }

    public int currentBarLength() {
        return BAR_LENGTH;
    }
}

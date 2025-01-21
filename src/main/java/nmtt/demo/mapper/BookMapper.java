package nmtt.demo.mapper;

import nmtt.demo.dto.request.Book.BookCreationRequest;
import nmtt.demo.dto.request.Book.BookUpdateRequest;
import nmtt.demo.dto.response.Book.BookResponse;
import nmtt.demo.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {
    Book toBook(BookCreationRequest request);
    BookResponse toBookResponse(Book book);
    void updateBook(@MappingTarget Book book, BookUpdateRequest request);
}

package bbr.bbrinvoiceservice.repository;

import bbr.bbrinvoiceservice.entity.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeRepository extends JpaRepository<Code, Integer> {
}

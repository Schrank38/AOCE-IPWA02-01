// GeisternetzRepository.java
package com.sheasepherd.ghostnet.repository;

import com.sheasepherd.ghostnet.model.Geisternetz;
import com.sheasepherd.ghostnet.model.NetzStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GeisternetzRepository extends JpaRepository<Geisternetz, Long> {
    List<Geisternetz> findByStatusIn(List<NetzStatus> statusList);
}
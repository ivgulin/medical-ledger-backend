package com.mokujin.government.repository;

import com.mokujin.government.model.entity.KnownIdentity;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface KnownIdentityRepository extends PagingAndSortingRepository<KnownIdentity, Integer> {

    Optional<KnownIdentity> findByNationalNumber_Number(String nationalNumber);

}

package com.zsecurity.demo.repositories;

import com.zsecurity.demo.entity.UserAddressDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAddressRepo extends JpaRepository<UserAddressDetails,Integer>{
}

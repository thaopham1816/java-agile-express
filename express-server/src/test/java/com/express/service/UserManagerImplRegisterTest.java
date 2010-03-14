package com.express.service;

import com.express.dao.UserDao;
import com.express.domain.User;
import com.express.service.dto.UserDto;
import com.express.service.mapping.DomainFactory;
import com.express.service.mapping.RemoteObjectFactory;
import com.express.service.notification.NotificationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.unitils.UnitilsJUnit4;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

public class UserManagerImplRegisterTest {
   static final String USERNAME = "test@test.com";
   static final String PASSWORD = "password";
   @Mock
   RemoteObjectFactory mockRemoteObjectFactory;
   @Mock
   UserDao mockUserDao;
   @Mock
   PasswordEncoder mockPasswordEncoder;
   @Mock
   DomainFactory mockDomainFactory;
   @Mock
   NotificationService mockNotificationService;
   
   UserManager userManager;
   
   @Before
   public void setUp() {
      MockitoAnnotations.initMocks(this);
      userManager = new UserManagerImpl(mockUserDao, mockPasswordEncoder,mockRemoteObjectFactory,
               mockDomainFactory, mockNotificationService);
   }
   
   @Test
   public void shouldRegisterNewUser(){
      UserDto dto = new UserDto();
      dto.setEmail(USERNAME);
      dto.setPassword(PASSWORD);
      User user = new User();
      user.setEmail(USERNAME);
      user.setPassword(PASSWORD);
      user.setActive(true);
      given(mockUserDao.findByUsername(USERNAME)).willThrow(new ObjectRetrievalFailureException(User.class,USERNAME));
      given(mockDomainFactory.createUser(dto)).willReturn(user);
      given(mockPasswordEncoder.encodePassword(PASSWORD, USERNAME)).willReturn("rubbish");
      mockUserDao.save(user);
      mockNotificationService.sendConfirmationNotification(user);
      
      userManager.register(dto);
   }

   @Test
   public void shouldConfirmRegistration() {
      User user = new User();
      given(mockUserDao.findById(1l)).willReturn(user);
      userManager.confirmRegistration(1l);
      assertTrue(user.getActive());
   }


}

package com.lul.Stydu4;

import com.lul.Stydu4.controller.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Stydu4ApplicationTests {

    @InjectMocks
    private AuthenticationController authenticationController;

    @InjectMocks
    private PartTestController partTestController;

    @InjectMocks
    private PermissionController permissionController;

    @InjectMocks
    private RoleController roleController;

    @InjectMocks
    private TestController testController;

    @InjectMocks
    private UserController userController;

	@Test
	void contextLoads() {

        Assertions.assertNotNull(authenticationController);
        Assertions.assertNotNull(partTestController);
        Assertions.assertNotNull(permissionController);
        Assertions.assertNotNull(roleController);
        Assertions.assertNotNull(testController);
        Assertions.assertNotNull(userController);

	}

}

package com.lul.Stydu4;

import com.lul.Stydu4.controller.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // ✅ Use test profile to use H2 instead of MySQL
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
        // Context loading test - verify Spring application context loads successfully
        Assertions.assertNotNull(authenticationController);
        Assertions.assertNotNull(partTestController);
        Assertions.assertNotNull(permissionController);
        Assertions.assertNotNull(roleController);
        Assertions.assertNotNull(testController);
        Assertions.assertNotNull(userController);
	}

}

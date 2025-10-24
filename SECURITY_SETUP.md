# Security Configuration Guide

## ⚠️ IMPORTANT SECURITY NOTICE

This project now uses **environment variables** for sensitive credentials. Never commit `.env` file to Git!

## Setup Instructions

### 1. Create Your Environment File

```bash
# Copy the example file
cp .env.example .env
```

### 2. Update Credentials in `.env`

Replace placeholder values with your actual credentials:

#### Database Configuration
```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=stydu5
DB_USERNAME=root
DB_PASSWORD=your_actual_password
```

#### Google OAuth2 (Get from [Google Console](https://console.cloud.google.com/apis/credentials))
```env
GOOGLE_CLIENT_ID=your_actual_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_actual_client_secret
```

#### JWT Secret Key (Generate a secure random key)
```bash
# Generate secure JWT key (Linux/Mac)
openssl rand -base64 64

# Or use online generator: https://www.grc.com/passwords.htm
```

```env
JWT_SIGNER_KEY=your_generated_64_char_secret_key
JWT_VALID_DURATION=36000
JWT_REFRESHABLE_DURATION=36000
```

#### Stripe API (Get from [Stripe Dashboard](https://dashboard.stripe.com/test/apikeys))
```env
STRIPE_API_KEY=sk_test_your_actual_stripe_key
```

### 3. Load Environment Variables

**Option A: Using IntelliJ IDEA / VS Code**
- IntelliJ: Run → Edit Configurations → Environment Variables → Load from file `.env`
- VS Code: Install "DotENV" extension

**Option B: Using Terminal (Development)**

**Windows (PowerShell):**
```powershell
# Install dotenv-cli
npm install -g dotenv-cli

# Run with env vars
dotenv -e .env -- ./mvnw spring-boot:run
```

**Linux/Mac:**
```bash
# Export manually
export $(cat .env | xargs)

# Or use dotenv-cli
npm install -g dotenv-cli
dotenv -e .env -- ./mvnw spring-boot:run
```

**Option C: Using Spring Boot Dev Tools (Recommended)**

Add to `pom.xml`:
```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

This automatically loads `.env` file on startup!

### 4. Verify Configuration

Run the application and check logs:
```bash
./mvnw spring-boot:run
```

Look for successful connections to:
- ✅ MySQL Database
- ✅ Redis
- ✅ Stripe API initialization

## 🔒 Security Best Practices

### DO ✅
- ✅ Keep `.env` in `.gitignore`
- ✅ Use different credentials for dev/staging/prod
- ✅ Rotate secrets regularly (every 90 days)
- ✅ Use strong, random JWT keys (min 64 characters)
- ✅ Share `.env.example` (without real values)
- ✅ Use environment-specific files (`.env.dev`, `.env.prod`)

### DON'T ❌
- ❌ Commit `.env` to Git
- ❌ Share credentials via email/chat
- ❌ Use production credentials in development
- ❌ Hardcode secrets in source code
- ❌ Reuse the same JWT key across environments

## 🔑 Credential Management

### Development Environment
- Use test/sandbox keys from providers
- Stripe: Use `sk_test_...` keys
- Google OAuth: Create separate OAuth app for dev

### Production Environment
- Use production keys
- Store in secure secret management (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault)
- Enable key rotation
- Monitor credential usage

## 📝 Current Credentials Status

**⚠️ ACTION REQUIRED:**

The following credentials were previously exposed in Git history:

1. **Google OAuth Client Secret** - ⚠️ REVOKE and regenerate
   - Go to: https://console.cloud.google.com/apis/credentials
   - Delete old OAuth 2.0 Client ID
   - Create new credentials

2. **Stripe API Key** - ⚠️ REVOKE and regenerate
   - Go to: https://dashboard.stripe.com/test/apikeys
   - Delete exposed test key
   - Generate new restricted key

3. **JWT Signer Key** - ⚠️ Generate new key
   ```bash
   openssl rand -base64 64
   ```

## 🚀 Quick Start (After Setup)

```bash
# 1. Copy environment file
cp .env.example .env

# 2. Edit .env with your credentials
nano .env  # or use your favorite editor

# 3. Install dependencies
./mvnw clean install

# 4. Run application
./mvnw spring-boot:run

# 5. Verify at http://localhost:8080
```

## 📚 Additional Resources

- [Spring Boot External Config](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [12 Factor App - Config](https://12factor.net/config)
- [OWASP Secrets Management](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)

## 🆘 Troubleshooting

**Problem:** Application fails to start with "Could not resolve placeholder"

**Solution:** 
```bash
# Check .env file exists
ls -la .env

# Verify environment variables are loaded
printenv | grep STRIPE
printenv | grep GOOGLE
```

**Problem:** Google OAuth login fails

**Solution:**
- Verify `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` are correct
- Check authorized redirect URIs in Google Console
- Ensure credentials are not revoked

**Problem:** Stripe payment fails

**Solution:**
- Verify `STRIPE_API_KEY` starts with `sk_test_` for development
- Check Stripe Dashboard for API key status
- Ensure webhook endpoints are configured

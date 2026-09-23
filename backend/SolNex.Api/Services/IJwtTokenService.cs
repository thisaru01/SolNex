using SolNex.Api.Models;

namespace SolNex.Api.Services;

public interface IJwtTokenService
{
    (string Token, DateTime ExpiresAt) CreateToken(User user);
}
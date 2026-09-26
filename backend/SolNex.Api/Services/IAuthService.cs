using SolNex.Api.DTOs;

namespace SolNex.Api.Services;

public interface IAuthService
{
    Task<LoginResponse?> LoginAsync(LoginRequest request, CancellationToken cancellationToken = default);
    Task<RegisteredUserResponse> RegisterProsumerAsync(RegisterUserRequest request, CancellationToken cancellationToken = default);
}
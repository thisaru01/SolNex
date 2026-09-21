using SolNex.Api.DTOs;

namespace SolNex.Api.Services;

public interface IUserService
{
    Task<RegisteredUserResponse> RegisterProsumerAsync(
        RegisterUserRequest request,
        CancellationToken cancellationToken = default);
}
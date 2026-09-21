using Microsoft.AspNetCore.Mvc;
using SolNex.Api.DTOs;
using SolNex.Api.Services;

namespace SolNex.Api.Controllers;

[ApiController]
[Route("api/users")]
public sealed class UsersController : ControllerBase
{
    private readonly IUserService _userService;

    public UsersController(IUserService userService)
    {
        _userService = userService;
    }

    [HttpPost("register")]
    [ProducesResponseType(typeof(RegisteredUserResponse), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status409Conflict)]
    public async Task<IActionResult> Register(
        RegisterUserRequest request,
        CancellationToken cancellationToken)
    {
        try
        {
            var user = await _userService.RegisterProsumerAsync(request, cancellationToken);
            return StatusCode(StatusCodes.Status201Created, user);
        }
        catch (InvalidOperationException exception)
        {
            return Conflict(new { message = exception.Message });
        }
    }
}

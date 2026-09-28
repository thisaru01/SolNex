using System.Security.Claims;

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using SolNex.Api.DTOs.Transactions;

using SolNex.Api.Services.Transactions;

namespace SolNex.Api.Controllers.Transactions;

[ApiController]
[Route("api/transactions")]
public class TransactionsController :
    ControllerBase
{
    private readonly
        ITransactionService
        _transactionService;

    // Initializes the transaction API.
    public TransactionsController(
        ITransactionService transactionService)
    {
        _transactionService =
            transactionService;
    }

    // Grid Operator:
    // Gets active transactions.
    [Authorize(
        Roles = "GridOperator")]
    [HttpGet("pending")]
    public async Task<IActionResult>
        GetPending()
    {
        return Ok(
            await _transactionService
                .GetPendingAsync());
    }

    // Grid Operator:
    // Gets completed history.
    [Authorize(
        Roles = "GridOperator")]
    [HttpGet("completed")]
    public async Task<IActionResult>
        GetCompleted()
    {
        return Ok(
            await _transactionService
                .GetCompletedAsync());
    }

    // Prosumer:
    // Gets active approved transaction records.
    [Authorize(
        Roles = "Prosumer")]
    [HttpGet("prosumer/active")]
    public async Task<IActionResult>
        GetActiveForProsumer()
    {
        var prosumerNic =
            User.FindFirstValue(
                ClaimTypes
                    .NameIdentifier);

        if (
            string.IsNullOrWhiteSpace(
                prosumerNic))
        {
            return Unauthorized(
                new
                {
                    message =
                        "Unable to identify the logged-in Prosumer."
                });
        }

        try
        {
            var transactions =
                await _transactionService
                    .GetActiveForProsumerAsync(
                        prosumerNic);

            return Ok(
                transactions);
        }
        catch (
            ArgumentException ex)
        {
            return BadRequest(
                new
                {
                    message =
                        ex.Message
                });
        }
    }

    // Prosumer:
    // Gets the secure transaction
    // belonging to one reservation.
    [Authorize(
        Roles = "Prosumer")]
    [HttpGet(
        "reservation/{reservationId}")]
    public async Task<IActionResult>
        GetByReservationId(
            string reservationId)
    {
        var prosumerNic =
            User.FindFirstValue(
                ClaimTypes
                    .NameIdentifier);

        if (
            string.IsNullOrWhiteSpace(
                prosumerNic))
        {
            return Unauthorized(
                new
                {
                    message =
                        "Unable to identify the logged-in Prosumer."
                });
        }

        try
        {
            var transaction =
                await _transactionService
                    .GetByReservationIdAsync(
                        reservationId,
                        prosumerNic);

            if (
                transaction ==
                null)
            {
                return NotFound(
                    new
                    {
                        message =
                            "No transaction exists for this reservation."
                    });
            }

            return Ok(
                transaction);
        }
        catch (
            UnauthorizedAccessException)
        {
            return Forbid();
        }
        catch (
            ArgumentException ex)
        {
            return BadRequest(
                new
                {
                    message =
                        ex.Message
                });
        }
    }

    // Grid Operator:
    // Gets one transaction.
    [Authorize(
        Roles = "GridOperator")]
    [HttpGet("{id}")]
    public async Task<IActionResult>
        GetById(
            string id)
    {
        var transaction =
            await _transactionService
                .GetByIdAsync(
                    id);

        if (
            transaction ==
            null)
        {
            return NotFound(
                new
                {
                    message =
                        $"Transaction with ID {id} was not found."
                });
        }

        return Ok(
            transaction);
    }

    // Grid Operator:
    // Verifies scanned QR.
    [Authorize(
        Roles = "GridOperator")]
    [HttpPost("verify")]
    public async Task<IActionResult>
        Verify(
            [FromBody]
            VerifyTransactionDto request)
    {
        if (
            !ModelState.IsValid)
        {
            return BadRequest(
                ModelState);
        }

        try
        {
            var transaction =
                await _transactionService
                    .VerifyAsync(
                        request.QrToken,
                        request.OperatorNic);

            return Ok(
                transaction);
        }
        catch (
            KeyNotFoundException ex)
        {
            return NotFound(
                new
                {
                    message =
                        ex.Message
                });
        }
        catch (
            InvalidOperationException ex)
        {
            return Conflict(
                new
                {
                    message =
                        ex.Message
                });
        }
        catch (
            ArgumentException ex)
        {
            return BadRequest(
                new
                {
                    message =
                        ex.Message
                });
        }
    }

    // Grid Operator:
    // Completes energy transfer.
    [Authorize(
        Roles = "GridOperator")]
    [HttpPost("{id}/complete")]
    public async Task<IActionResult>
        Complete(
            string id,
            [FromBody]
            CompleteTransactionDto request)
    {
        if (
            !ModelState.IsValid)
        {
            return BadRequest(
                ModelState);
        }

        try
        {
            var transaction =
                await _transactionService
                    .CompleteAsync(
                        id,
                        request.OperatorNic);

            return Ok(
                transaction);
        }
        catch (
            KeyNotFoundException ex)
        {
            return NotFound(
                new
                {
                    message =
                        ex.Message
                });
        }
        catch (
            InvalidOperationException ex)
        {
            return Conflict(
                new
                {
                    message =
                        ex.Message
                });
        }
        catch (
            ArgumentException ex)
        {
            return BadRequest(
                new
                {
                    message =
                        ex.Message
                });
        }
    }
}
/*
 * File: TransactionsController.cs
 * Component: Operator, Approval & Energy Transaction
 * Description:
 * Handles transaction viewing for Backoffice and Grid Operators,
 * Prosumer transaction access, QR verification, and completion
 * of energy transfers by Grid Operators.
 */

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

    /*
     * Initializes the transaction controller
     * with the transaction service.
     */
    public TransactionsController(
        ITransactionService transactionService)
    {
        _transactionService =
            transactionService;
    }

    /*
     * Backoffice / Grid Operator:
     * Gets active pending transactions.
     */
    [Authorize(
        Roles = "Backoffice,GridOperator")]
    [HttpGet("pending")]
    public async Task<IActionResult>
        GetPending()
    {
        // Retrieve all pending transaction records.
        var transactions =
            await _transactionService
                .GetPendingAsync();

        return Ok(
            transactions);
    }

    /*
     * Backoffice / Grid Operator:
     * Gets completed transaction history.
     */
    [Authorize(
        Roles = "Backoffice,GridOperator")]
    [HttpGet("completed")]
    public async Task<IActionResult>
        GetCompleted()
    {
        // Retrieve completed transaction records.
        var transactions =
            await _transactionService
                .GetCompletedAsync();

        return Ok(
            transactions);
    }

    /*
     * Prosumer:
     * Gets active approved transaction records
     * belonging to the logged-in Prosumer.
     */
    [Authorize(
        Roles = "Prosumer")]
    [HttpGet("prosumer/active")]
    public async Task<IActionResult>
        GetActiveForProsumer()
    {
        // Read the Prosumer NIC from the authenticated JWT.
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

    /*
     * Prosumer:
     * Gets the secure transaction belonging
     * to one of the Prosumer's reservations.
     */
    [Authorize(
        Roles = "Prosumer")]
    [HttpGet(
        "reservation/{reservationId}")]
    public async Task<IActionResult>
        GetByReservationId(
            string reservationId)
    {
        // Read the logged-in Prosumer NIC from the JWT.
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

    /*
     * Backoffice / Grid Operator:
     * Gets details of one transaction.
     */
    [Authorize(
        Roles = "Backoffice,GridOperator")]
    [HttpGet("{id}")]
    public async Task<IActionResult>
        GetById(
            string id)
    {
        // Retrieve the requested transaction by its identifier.
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

    /*
     * Grid Operator only:
     * Verifies a scanned Prosumer QR code
     * against the server transaction data.
     */
    [Authorize(
        Roles = "GridOperator")]
    [HttpPost("verify")]
    public async Task<IActionResult>
        Verify(
            [FromBody]
            VerifyTransactionDto request)
    {
        // Validate the QR verification request.
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

    /*
     * Grid Operator only:
     * Completes a verified energy transfer.
     */
    [Authorize(
        Roles = "GridOperator")]
    [HttpPost("{id}/complete")]
    public async Task<IActionResult>
        Complete(
            string id,
            [FromBody]
            CompleteTransactionDto request)
    {
        // Validate the transaction completion request.
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
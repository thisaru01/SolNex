/*
 * File: TransactionRepository.cs
 * Component: Transaction Repositories
 * Description:
 * Implements MongoDB persistence operations for energy transactions.
 */

using MongoDB.Bson;
using MongoDB.Driver;

using SolNex.Api.Data;
using SolNex.Api.Models;

namespace SolNex.Api.Repositories.Transactions;

public class TransactionRepository :
    ITransactionRepository
{
    private readonly
        IMongoCollection<EnergyTransaction>
        _transactions;

    public TransactionRepository(
        MongoDbContext dbContext)
    {
        // Initializes the transaction collection and required unique indexes.
        _transactions =
            dbContext.EnergyTransactions;

        var indexes =
            new[]
            {
                new CreateIndexModel<
                    EnergyTransaction>(
                    Builders<
                        EnergyTransaction>
                        .IndexKeys
                        .Ascending(
                            transaction =>
                                transaction
                                    .TransactionId),

                    new CreateIndexOptions
                    {
                        Unique = true
                    }),

                new CreateIndexModel<
                    EnergyTransaction>(
                    Builders<
                        EnergyTransaction>
                        .IndexKeys
                        .Ascending(
                            transaction =>
                                transaction
                                    .ReservationId),

                    new CreateIndexOptions
                    {
                        Unique = true
                    }),

                new CreateIndexModel<
                    EnergyTransaction>(
                    Builders<
                        EnergyTransaction>
                        .IndexKeys
                        .Ascending(
                            transaction =>
                                transaction
                                    .QrToken),

                    new CreateIndexOptions
                    {
                        Unique = true
                    })
            };

        _transactions
            .Indexes
            .CreateMany(
                indexes);
    }

    public async Task<
        EnergyTransaction?>
        GetByIdOrTransactionIdAsync(
            string id)
    {
        // Gets a transaction using either MongoDB ID or public Transaction ID.
        if (
            ObjectId.TryParse(
                id,
                out _))
        {
            var byObjectId =
                await _transactions
                    .Find(
                        transaction =>
                            transaction.Id ==
                            id)
                    .FirstOrDefaultAsync();

            if (
                byObjectId !=
                null)
            {
                return byObjectId;
            }
        }

        return await _transactions
            .Find(
                transaction =>
                    transaction
                        .TransactionId ==
                    id)
            .FirstOrDefaultAsync();
    }

    public async Task<
        EnergyTransaction?>
        GetByQrTokenAsync(
            string qrToken)
    {
        // Finds the transaction whose secure token was scanned from the QR code.
        return await _transactions
            .Find(
                transaction =>
                    transaction.QrToken ==
                    qrToken)
            .FirstOrDefaultAsync();
    }

    public async Task<
        EnergyTransaction?>
        GetByReservationIdAsync(
            string reservationId)
    {
        // Finds the transaction generated for one approved reservation.
        return await _transactions
            .Find(
                transaction =>
                    transaction
                        .ReservationId ==
                    reservationId)
            .FirstOrDefaultAsync();
    }

    public async Task<
        IEnumerable<EnergyTransaction>>
        GetPendingAsync()
    {
        // Returns transactions that are awaiting verification or completion.
        var filter =
            Builders<
                EnergyTransaction>
                .Filter
                .In(
                    transaction =>
                        transaction.Status,

                    new[]
                    {
                        TransactionStatus.Pending,
                        TransactionStatus.Verified
                    });

        return await _transactions
            .Find(filter)
            .SortByDescending(
                transaction =>
                    transaction.VerifiedAt)
            .ToListAsync();
    }

    public async Task<
        IEnumerable<EnergyTransaction>>
        GetCompletedAsync()
    {
        // Returns completed energy transfers.
        return await _transactions
            .Find(
                transaction =>
                    transaction.Status ==
                    TransactionStatus.Completed)
            .SortByDescending(
                transaction =>
                    transaction.CompletedAt)
            .ToListAsync();
    }

    public async Task<
        IEnumerable<EnergyTransaction>>
        GetActiveByProsumerNicAsync(
            string nic)
    {
        // Returns active approved transactions belonging to one Prosumer.
        var statusFilter =
            Builders<
                EnergyTransaction>
                .Filter
                .In(
                    transaction =>
                        transaction.Status,

                    new[]
                    {
                        TransactionStatus.Pending,
                        TransactionStatus.Verified
                    });

        var filter =
            Builders<
                EnergyTransaction>
                .Filter
                .And(

                    Builders<
                        EnergyTransaction>
                        .Filter
                        .Eq(
                            transaction =>
                                transaction.Nic,

                            nic),

                    statusFilter
                );

        return await _transactions
            .Find(filter)
            .SortByDescending(
                transaction =>
                    transaction
                        .TransactionId)
            .ToListAsync();
    }

    public async Task CreateAsync(
        EnergyTransaction transaction)
    {
        // Persists a new transaction record.
        await _transactions
            .InsertOneAsync(
                transaction);
    }

    public async Task UpdateAsync(
        EnergyTransaction transaction)
    {
        // Replaces the persisted transaction with its updated state.
        if (
            string.IsNullOrWhiteSpace(
                transaction.Id))
        {
            throw new InvalidOperationException(
                "Transaction database ID is missing.");
        }

        await _transactions
            .ReplaceOneAsync(
                current =>
                    current.Id ==
                    transaction.Id,

                transaction);
    }
}
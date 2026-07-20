import { z } from 'zod';

export const loginSchema = z.object({
  email: z.string().email('Enter a valid email'),
  password: z.string().min(1, 'Password is required'),
});
export type LoginFormValues = z.infer<typeof loginSchema>;

export const registerSchema = z.object({
  fname: z.string().min(1, 'First name is required'),
  lname: z.string().min(1, 'Last name is required'),
  email: z.string().email('Enter a valid email'),
  phoneNumber: z.string().regex(/^[+]?[0-9]{7,15}$/, 'Enter a valid phone number'),
  password: z.string().min(8, 'Must be at least 8 characters'),
});
export type RegisterFormValues = z.infer<typeof registerSchema>;

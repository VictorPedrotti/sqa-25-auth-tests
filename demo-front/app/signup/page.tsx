"use client";

import React from "react";
import { Form, Input, Button, addToast } from "@heroui/react";
import { useRouter } from "next/navigation";
import authService from "@/service/auth";
import { validateEmail } from "@/utils/email";
import { validatePassword } from "@/utils/password";
import { isAxiosError } from "axios";

export default function SignUp() {
  const [isPasswordInvalid, setIsPasswordInvalid] = React.useState(false);
  const [isEmailInvalid, setIsEmailInvalid] = React.useState(false);

  const [emailErrorMessage, setEmailErrorMessage] = React.useState<
    string | null
  >(null);
  const [passwordErrorMessage, setPasswordErrorMessage] = React.useState<
    string | null
  >(null);

  const router = useRouter();

  function clearErrors() {
    setIsPasswordInvalid(false);
    setIsEmailInvalid(false);
  }

  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <h1 className="text-3xl font-bold mb-8">Criação de conta!</h1>
      <Form
        validationBehavior="aria"
        className="w-full max-w-xs flex flex-col gap-4"
        onSubmit={async (e) => {
          e.preventDefault();
          let data = Object.fromEntries(new FormData(e.currentTarget));
          if (!validateEmail(data.email as string)) {
            setIsEmailInvalid(true);
            setEmailErrorMessage("E-mail inválido");
            return;
          }
          if (!validatePassword(data.password as string)) {
            setIsPasswordInvalid(true);
            setPasswordErrorMessage("Senha inválida"); //  Se o usuário fornecer uma senha que não atenda esses critérios, mostrar a mensagem: “Senha não atende aos critérios mínimos de segurança”.
            return;
          }
          try {
            const response = await authService.signUp(
              data.email as string,
              data.password as string
            );
            if (response.id) {
              addToast({
                title: "Cadastro realizado com sucesso",
                color: "success",
              });
              return router.push("/auth/home");
            }
          } catch (e) {
            if (isAxiosError(e)) {
              if (e.response?.status === 409) {
                setEmailErrorMessage("E-mail já cadastrado");
                setIsEmailInvalid(true);
                return;
              }
              console.log(e.response?.data);
            } else {
              console.log(e);
            }
          }
        }}
      >
        <Input
          onValueChange={() => clearErrors()}
          isRequired
          label="E-mail"
          labelPlacement="outside"
          name="email"
          placeholder="Digite seu e-mail"
          type="email"
          isInvalid={isEmailInvalid}
          errorMessage={emailErrorMessage}
        />

        <Input
          onValueChange={() => clearErrors()}
          isRequired
          label="Senha"
          labelPlacement="outside"
          name="password"
          placeholder="Digite sua senha"
          type="password"
          isInvalid={isPasswordInvalid}
          errorMessage={passwordErrorMessage}
        />
        <div className="flex flex-col gap-4 w-full h-auto">
          <Button className="h-10" color="primary" type="submit">
            Criar conta
          </Button>
          <Button
            className="h-10"
            type="button"
            color="secondary"
            variant="ghost"
            onPress={() => router.push("/")}
          >
            Acessar com conta existente
          </Button>
        </div>
      </Form>
    </main>
  );
}
